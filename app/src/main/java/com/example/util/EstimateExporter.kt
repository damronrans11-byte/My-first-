package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SecondaryServiceItem(
  val id: String,
  val name: String,
  val description: String,
  val unitPrice: Double,
  var quantity: Int = 0,
  var isSelected: Boolean = false
)

object EstimateExporter {

  fun getDefaultSecondaryServices(): List<SecondaryServiceItem> {
    return listOf(
      SecondaryServiceItem(
        id = "srv_consulting",
        name = "Secondary Advisory & Strategy Call",
        description = "One-on-one consulting call regarding telecom integration & client engagement",
        unitPrice = 175.00,
        quantity = 2,
        isSelected = true
      ),
      SecondaryServiceItem(
        id = "srv_sms_automation",
        name = "Automated SMS Alert & Webhook Setup",
        description = "Configuration of priority SMS alerting workflows and CRM communication sync",
        unitPrice = 320.00,
        quantity = 1,
        isSelected = true
      ),
      SecondaryServiceItem(
        id = "srv_frequency_report",
        name = "Comprehensive Interaction Frequency Audit",
        description = "In-depth multi-week call and message interaction velocity analysis report",
        unitPrice = 140.00,
        quantity = 1,
        isSelected = true
      ),
      SecondaryServiceItem(
        id = "srv_vip_dispatch",
        name = "24/7 Priority Inbound Alert Dispatch",
        description = "Dedicated secondary line routing and heads-up push alert SLA package",
        unitPrice = 450.00,
        quantity = 1,
        isSelected = false
      ),
      SecondaryServiceItem(
        id = "srv_telecom_audit",
        name = "Telecom & Mobile Line Expense Optimization",
        description = "Analysis of carrier line costs and cash flow expense reduction",
        unitPrice = 95.00,
        quantity = 1,
        isSelected = false
      )
    )
  }

  fun generateEstimateText(
    estimateNumber: String,
    userCellNumber: String,
    clientName: String,
    clientPhone: String,
    selectedItems: List<SecondaryServiceItem>,
    discountPercent: Double = 0.0,
    notes: String = "",
    cashAppTag: String = ""
  ): String {
    val date = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
    val subtotal = selectedItems.sumOf { it.unitPrice * it.quantity }
    val discountAmount = subtotal * (discountPercent / 100.0)
    val total = subtotal - discountAmount

    val sb = StringBuilder()
    sb.append("========================================================\n")
    sb.append("         ESTIMATE FOR SECONDARY SERVICES               \n")
    sb.append("                 I Spy Communications                  \n")
    sb.append("========================================================\n\n")
    sb.append("Estimate Number : $estimateNumber\n")
    sb.append("Date Issued     : $date\n")
    sb.append("Provider Line   : $userCellNumber\n\n")
    sb.append("PREPARED FOR:\n")
    sb.append("  Client Name   : $clientName\n")
    sb.append("  Client Phone  : $clientPhone\n\n")
    sb.append("--------------------------------------------------------\n")
    sb.append(String.format("%-32s %4s %9s %9s\n", "SECONDARY SERVICE", "QTY", "RATE", "TOTAL"))
    sb.append("--------------------------------------------------------\n")

    selectedItems.forEach { item ->
      val itemTotal = item.unitPrice * item.quantity
      val title = if (item.name.length > 30) item.name.take(28) + ".." else item.name
      sb.append(String.format("%-32s %4d $%8.2f $%8.2f\n", title, item.quantity, item.unitPrice, itemTotal))
      sb.append("  * ${item.description}\n")
    }

    sb.append("--------------------------------------------------------\n")
    sb.append(String.format("%-46s $%9.2f\n", "SUBTOTAL:", subtotal))
    if (discountPercent > 0) {
      sb.append(String.format("%-46s-$%9.2f (%s%%)\n", "DISCOUNT APPLIED:", discountAmount, discountPercent))
    }
    sb.append("========================================================\n")
    sb.append(String.format("%-46s $%9.2f\n", "TOTAL ESTIMATED AMOUNT:", total))
    sb.append("========================================================\n\n")

    if (cashAppTag.isNotBlank()) {
      val tag = if (cashAppTag.startsWith("$")) cashAppTag else "$$cashAppTag"
      val cleanTag = tag.removePrefix("$")
      sb.append("PAYMENT INFORMATION (CASH APP):\n")
      sb.append("  Settle via Cash App: $tag\n")
      sb.append("  Direct Payment Link: https://cash.app/$$cleanTag\n\n")
    }

    if (notes.isNotBlank()) {
      sb.append("TERMS & SPECIAL NOTES:\n")
      sb.append("$notes\n\n")
    }

    sb.append("VALIDITY: This secondary services estimate is valid for 30 days.\n")
    sb.append("LICENSED PRODUCT & LEGAL NOTICE:\n")
    sb.append("  I Spy is a proprietary licensed software product owned exclusively by its creator.\n")
    sb.append("  This software shall not be used in any illegal activity whatsoever, manipulated or\n")
    sb.append("  changed in anyway, or reproduced for one's own monetary gain. All rights reserved.\n")
    sb.append("Generated with I Spy Communications & Profit Suite.\n")
    return sb.toString()
  }

  fun generateEstimateHtml(
    estimateNumber: String,
    userCellNumber: String,
    clientName: String,
    clientPhone: String,
    selectedItems: List<SecondaryServiceItem>,
    discountPercent: Double = 0.0,
    notes: String = "",
    cashAppTag: String = ""
  ): String {
    val date = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
    val subtotal = selectedItems.sumOf { it.unitPrice * it.quantity }
    val discountAmount = subtotal * (discountPercent / 100.0)
    val total = subtotal - discountAmount

    val itemsHtml = selectedItems.joinToString("") { item ->
      val itemTotal = item.unitPrice * item.quantity
      """
      <tr>
        <td style="padding: 12px; border-bottom: 1px solid #e2e8f0;">
          <strong>${item.name}</strong><br>
          <span style="font-size: 12px; color: #64748b;">${item.description}</span>
        </td>
        <td style="padding: 12px; border-bottom: 1px solid #e2e8f0; text-align: center;">${item.quantity}</td>
        <td style="padding: 12px; border-bottom: 1px solid #e2e8f0; text-align: right;">$${"%.2f".format(item.unitPrice)}</td>
        <td style="padding: 12px; border-bottom: 1px solid #e2e8f0; text-align: right; font-weight: bold;">$${"%.2f".format(itemTotal)}</td>
      </tr>
      """.trimIndent()
    }

    return """
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Estimate $estimateNumber</title>
        <style>
          body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 0; padding: 24px; color: #0f172a; background: #f8fafc; }
          .card { max-width: 680px; margin: 0 auto; background: #ffffff; border-radius: 16px; padding: 32px; box-shadow: 0 4px 12px rgba(0,0,0,0.06); }
          .header { display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #e2e8f0; padding-bottom: 20px; }
          .brand { font-size: 24px; font-weight: 800; color: #4f46e5; }
          .badge { background: #e0e7ff; color: #3730a3; padding: 6px 12px; border-radius: 8px; font-size: 13px; font-weight: 700; }
          .details { margin: 24px 0; display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
          table { width: 100%; border-collapse: collapse; margin-top: 16px; }
          th { background: #f1f5f9; padding: 12px; text-align: left; font-size: 13px; color: #475569; }
          .total-box { margin-top: 24px; text-align: right; }
          .total-amount { font-size: 28px; font-weight: 800; color: #10b981; }
          .footer { margin-top: 32px; padding-top: 16px; border-top: 1px solid #e2e8f0; font-size: 12px; color: #94a3b8; text-align: center; }
        </style>
      </head>
      <body>
        <div class="card">
          <div class="header">
            <div>
              <div class="brand">I Spy</div>
              <div style="font-size: 13px; color: #64748b;">Secondary Services Quotation</div>
            </div>
            <div class="badge">Estimate #$estimateNumber</div>
          </div>
          <div class="details">
            <div>
              <strong style="color: #64748b; font-size: 12px;">PREPARED BY:</strong>
              <div>Line: $userCellNumber</div>
              <div>Date: $date</div>
            </div>
            <div>
              <strong style="color: #64748b; font-size: 12px;">PREPARED FOR:</strong>
              <div style="font-weight: 700;">$clientName</div>
              <div>$clientPhone</div>
            </div>
          </div>
          <table>
            <thead>
              <tr>
                <th>Secondary Service</th>
                <th style="text-align: center;">Qty</th>
                <th style="text-align: right;">Rate</th>
                <th style="text-align: right;">Amount</th>
              </tr>
            </thead>
            <tbody>
              $itemsHtml
            </tbody>
          </table>
          <div class="total-box">
            <div style="font-size: 14px; color: #64748b;">Subtotal: $${"%.2f".format(subtotal)}</div>
            ${if (discountPercent > 0) "<div style=\"font-size: 14px; color: #f43f5e;\">Discount ($discountPercent%): -$${"%.2f".format(discountAmount)}</div>" else ""}
            <div style="font-size: 14px; margin-top: 8px;">TOTAL ESTIMATE:</div>
            <div class="total-amount">$${"%.2f".format(total)}</div>
          </div>
          ${if (cashAppTag.isNotBlank()) {
            val tag = if (cashAppTag.startsWith("$")) cashAppTag else "$$cashAppTag"
            val cleanTag = tag.removePrefix("$")
            """
            <div style="margin-top: 24px; padding: 16px; background: #ecfdf5; border: 1px solid #a7f3d0; border-radius: 12px; display: flex; justify-content: space-between; align-items: center;">
              <div>
                <strong style="color: #065f46; font-size: 14px;">Remit Payment via Cash App</strong><br>
                <span style="font-size: 13px; color: #047857;">Recipient Cashtag: <strong>$tag</strong></span>
              </div>
              <a href="https://cash.app/$$cleanTag" style="background: #00D632; color: #ffffff; text-decoration: none; padding: 8px 16px; border-radius: 8px; font-weight: 700; font-size: 13px;">Pay on Cash App</a>
            </div>
            """.trimIndent()
          } else ""}
          ${if (notes.isNotBlank()) "<div style=\"margin-top: 20px; padding: 12px; background: #f8fafc; border-radius: 8px; font-size: 13px;\"><strong>Notes:</strong> $notes</div>" else ""}
          <div style="margin-top: 24px; padding: 14px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 10px; font-size: 11px; color: #64748b; line-height: 1.5; text-align: left;">
            <strong style="color: #334155;">PROPRIETARY LICENSED PRODUCT &amp; USER AGREEMENT:</strong><br>
            This document is issued by I Spy, an officially licensed software product owned solely by its creator.
            Under penalty of law, this software and its output shall NOT be used in any illegal activity whatsoever,
            nor manipulated or changed in anyway, or reproduced for one's own monetary gain. All rights reserved.
          </div>
          <div class="footer">
            Valid for 30 days. Exported on Android via I Spy.
          </div>
        </div>
      </body>
      </html>
    """.trimIndent()
  }

  /**
   * Downloads and saves the estimate to any Android device using MediaStore.Downloads (API 29+)
   * or standard public app Documents, with instant Share and View intents.
   */
  fun downloadEstimateToDevice(
    context: Context,
    estimateNumber: String,
    content: String,
    asHtml: Boolean = false
  ): Uri? {
    val extension = if (asHtml) "html" else "txt"
    val mimeType = if (asHtml) "text/html" else "text/plain"
    val fileName = "Estimate_${estimateNumber}_Secondary_Services.$extension"

    return try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
          put(MediaStore.Downloads.DISPLAY_NAME, fileName)
          put(MediaStore.Downloads.MIME_TYPE, mimeType)
          put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let { targetUri ->
          resolver.openOutputStream(targetUri)?.use { out ->
            out.write(content.toByteArray(Charsets.UTF_8))
            out.flush()
          }
        }
        uri
      } else {
        val docsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val file = File(docsDir, fileName)
        FileOutputStream(file).use { out ->
          out.write(content.toByteArray(Charsets.UTF_8))
          out.flush()
        }
        FileProvider.getUriForFile(
          context,
          "${context.packageName}.fileprovider",
          file
        )
      }
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  fun shareEstimateIntent(
    context: Context,
    estimateNumber: String,
    content: String,
    uri: Uri?
  ) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_SUBJECT, "Secondary Services Estimate #$estimateNumber")
      putExtra(Intent.EXTRA_TEXT, content)
      if (uri != null) {
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
    }
    context.startActivity(Intent.createChooser(shareIntent, "Download / Share Estimate via"))
  }
}
