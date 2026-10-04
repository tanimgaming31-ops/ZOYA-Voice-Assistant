package com.example.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ZoyaBillingManager(private val context: Context) : PurchasesUpdatedListener {
  private val TAG = "ZoyaBillingManager"

  private val _isPremiumTier = MutableStateFlow(false)
  val isPremiumTier: StateFlow<Boolean> = _isPremiumTier.asStateFlow()

  private lateinit var billingClient: BillingClient

  init {
    setupBillingClient()
  }

  private fun setupBillingClient() {
    billingClient = BillingClient.newBuilder(context)
      .setListener(this)
      .enablePendingPurchases()
      .build()

    billingClient.startConnection(object : BillingClientStateListener {
      override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
          Log.i(TAG, "Google Play Billing setup finished successfully.")
          queryActivePurchases()
        } else {
          Log.w(TAG, "Billing setup failed with code: ${billingResult.responseCode}")
        }
      }

      override fun onBillingServiceDisconnected() {
        Log.w(TAG, "Billing service disconnected. Attempting reconnect.")
      }
    })
  }

  override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
      for (purchase in purchases) {
        handlePurchase(purchase)
      }
    } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
      Log.i(TAG, "User canceled the purchase.")
    } else {
      Log.w(TAG, "Purchase update failed with code: ${billingResult.responseCode}")
    }
  }

  private fun handlePurchase(purchase: Purchase) {
    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
      _isPremiumTier.value = true
      Log.i(TAG, "Premium subscription active verified via Google Play Billing.")
    }
  }

  private fun queryActivePurchases() {
    billingClient.queryPurchasesAsync(
      QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
    ) { billingResult, purchases ->
      if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
        val active = purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
        _isPremiumTier.value = active
        Log.i(TAG, "Active premium subscription status: $active")
      }
    }
  }

  fun launchSubscriptionFlow(activity: Activity, skuId: String = "zoya_premium_monthly") {
    val productList = listOf(
      QueryProductDetailsParams.Product.newBuilder()
        .setProductId(skuId)
        .setProductType(BillingClient.ProductType.SUBS)
        .build()
    )

    val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

    billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
      if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
        val productDetails = productDetailsList[0]
        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return@queryProductDetailsAsync

        val productDetailsParamsList = listOf(
          BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offerToken)
            .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
          .setProductDetailsParamsList(productDetailsParamsList)
          .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
      } else {
        Log.w(TAG, "Product details query failed for subscription: ${billingResult.responseCode}")
      }
    }
  }
}
