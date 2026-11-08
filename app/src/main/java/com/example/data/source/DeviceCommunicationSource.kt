package com.example.data.source

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.example.data.entity.CommunicationEntity
import com.example.data.model.InteractionType

/**
 * Reads real calls and texts from THIS phone only, using the Android
 * CallLog and SMS content providers. The user must grant READ_CALL_LOG,
 * READ_SMS and READ_CONTACTS first; without them this returns
 * [ImportResult.PermissionMissing] instead of crashing or inventing data.
 *
 * Facebook Messenger has no Android content provider or public API, so
 * Messenger chats cannot be imported here. They are logged manually
 * in the app (see ContactPulseViewModel.triggerFacebookMessengerAlert).
 */
object DeviceCommunicationSource {

  /** Last 10 digits of a number, so "+1 (555) 329-8472" matches "5553298472". */
  fun normalizeNumber(raw: String?): String {
    val digits = raw.orEmpty().filter { it.isDigit() }
    return if (digits.length > 10) digits.takeLast(10) else digits
  }

  fun hasPermissions(context: Context): Boolean {
    val needed = listOf(
      Manifest.permission.READ_CALL_LOG,
      Manifest.permission.READ_SMS,
      Manifest.permission.READ_CONTACTS
    )
    return needed.all {
      ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
  }

  sealed interface ImportResult {
    data class Imported(val entries: List<CommunicationEntity>) : ImportResult
    data object PermissionMissing : ImportResult
  }

  /**
   * Read this phone's calls and texts. When [contactNumber] is given, only
   * entries for that number are returned (normalized); otherwise all recent
   * entries are returned, up to [limit] per source.
   */
  fun readDeviceHistory(
    context: Context,
    userCellNumber: String,
    contactNumber: String? = null,
    limit: Int = 500
  ): ImportResult {
    if (!hasPermissions(context)) return ImportResult.PermissionMissing

    val wanted = contactNumber?.let { normalizeNumber(it) }?.takeIf { it.isNotEmpty() }
    val entries = mutableListOf<CommunicationEntity>()
    entries += readCallLog(context, userCellNumber, wanted, limit)
    entries += readSms(context, userCellNumber, wanted, limit)
    return ImportResult.Imported(entries.sortedByDescending { it.timestamp })
  }

  private fun readCallLog(
    context: Context,
    userCellNumber: String,
    wanted: String?,
    limit: Int
  ): List<CommunicationEntity> {
    val out = mutableListOf<CommunicationEntity>()
    val projection = arrayOf(
      CallLog.Calls.NUMBER,
      CallLog.Calls.CACHED_NAME,
      CallLog.Calls.TYPE,
      CallLog.Calls.DATE,
      CallLog.Calls.DURATION
    )
    val cursor = try {
      context.contentResolver.query(
        CallLog.Calls.CONTENT_URI, projection, null, null,
        "${CallLog.Calls.DATE} DESC LIMIT $limit"
      )
    } catch (e: SecurityException) {
      return emptyList()
    } catch (e: Exception) {
      return emptyList()
    } ?: return emptyList()

    cursor.use {
      val numberIdx = it.getColumnIndex(CallLog.Calls.NUMBER)
      val nameIdx = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
      val typeIdx = it.getColumnIndex(CallLog.Calls.TYPE)
      val dateIdx = it.getColumnIndex(CallLog.Calls.DATE)
      val durationIdx = it.getColumnIndex(CallLog.Calls.DURATION)
      while (it.moveToNext()) {
        val rawNumber = if (numberIdx >= 0) it.getString(numberIdx).orEmpty() else ""
        val normalized = normalizeNumber(rawNumber)
        if (normalized.isEmpty()) continue
        if (wanted != null && normalized != wanted) continue
        val type = when (if (typeIdx >= 0) it.getInt(typeIdx) else CallLog.Calls.MISSED_TYPE) {
          CallLog.Calls.INCOMING_TYPE -> InteractionType.CALL_INCOMING
          CallLog.Calls.OUTGOING_TYPE -> InteractionType.CALL_OUTGOING
          CallLog.Calls.MISSED_TYPE -> InteractionType.CALL_MISSED
          CallLog.Calls.REJECTED_TYPE -> InteractionType.CALL_MISSED
          else -> InteractionType.CALL_INCOMING
        }
        val name = if (nameIdx >= 0) it.getString(nameIdx) else null
        out += CommunicationEntity(
          userCellNumber = userCellNumber,
          contactName = name?.takeIf { it.isNotBlank() }
            ?: resolveContactName(context, normalized) ?: rawNumber,
          contactNumber = rawNumber,
          type = type.name,
          messageBody = when (type) {
            InteractionType.CALL_MISSED -> "Missed Call"
            else -> "Voice Call"
          },
          callDurationSeconds = if (durationIdx >= 0) it.getInt(durationIdx) else 0,
          timestamp = if (dateIdx >= 0) it.getLong(dateIdx) else System.currentTimeMillis(),
          isRead = type != InteractionType.CALL_MISSED
        )
      }
    }
    return out
  }

  private fun readSms(
    context: Context,
    userCellNumber: String,
    wanted: String?,
    limit: Int
  ): List<CommunicationEntity> {
    val out = mutableListOf<CommunicationEntity>()
    // Inbox (received) and Sent are separate providers; read both.
    val sources = listOf(
      Telephony.Sms.Inbox.CONTENT_URI to InteractionType.SMS_INCOMING,
      Telephony.Sms.Sent.CONTENT_URI to InteractionType.SMS_OUTGOING
    )
    val projection = arrayOf(
      Telephony.Sms.ADDRESS,
      Telephony.Sms.BODY,
      Telephony.Sms.DATE,
      Telephony.Sms.READ
    )
    for ((uri, type) in sources) {
      val cursor = try {
        context.contentResolver.query(
          uri, projection, null, null,
          "${Telephony.Sms.DATE} DESC LIMIT $limit"
        )
      } catch (e: SecurityException) {
        continue
      } catch (e: Exception) {
        continue
      } ?: continue
      cursor.use {
        val addressIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
        val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
        val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)
        val readIdx = it.getColumnIndex(Telephony.Sms.READ)
        while (it.moveToNext()) {
          val rawNumber = if (addressIdx >= 0) it.getString(addressIdx).orEmpty() else ""
          val normalized = normalizeNumber(rawNumber)
          if (normalized.isEmpty()) continue
          if (wanted != null && normalized != wanted) continue
          out += CommunicationEntity(
            userCellNumber = userCellNumber,
            contactName = resolveContactName(context, normalized) ?: rawNumber,
            contactNumber = rawNumber,
            type = type.name,
            messageBody = if (bodyIdx >= 0) it.getString(bodyIdx).orEmpty() else "",
            callDurationSeconds = 0,
            timestamp = if (dateIdx >= 0) it.getLong(dateIdx) else System.currentTimeMillis(),
            isRead = if (readIdx >= 0) it.getInt(readIdx) == 1 else true
          )
        }
      }
    }
    return out
  }

  /** Look a number up in this phone's contacts; null when it is not saved. */
  fun resolveContactName(context: Context, normalizedNumber: String): String? =
    lookupContactName(context, normalizedNumber)

  /** Contact-name lookup that actually matches on the normalized number. */
  fun lookupContactName(context: Context, normalizedNumber: String): String? {
    if (normalizedNumber.isEmpty()) return null
    val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
    val projection = arrayOf(
      ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
      ContactsContract.CommonDataKinds.Phone.NUMBER
    )
    val cursor = try {
      context.contentResolver.query(uri, projection, null, null, null)
    } catch (e: SecurityException) {
      return null
    } catch (e: Exception) {
      return null
    } ?: return null
    cursor.use {
      val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
      val numberIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
      while (it.moveToNext()) {
        val stored = if (numberIdx >= 0) it.getString(numberIdx).orEmpty() else ""
        if (normalizeNumber(stored) == normalizedNumber) {
          return if (nameIdx >= 0) it.getString(nameIdx)?.takeIf { n -> n.isNotBlank() } else null
        }
      }
    }
    return null
  }
}
