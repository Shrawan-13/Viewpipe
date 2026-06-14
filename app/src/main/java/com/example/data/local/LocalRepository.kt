package com.example.data.local

class LocalRepository(
    private val historyDao: HistoryDao,
    private val watchLaterDao: WatchLaterDao,
    private val subscriptionDao: SubscriptionDao
) {
    val history = historyDao.getAllHistory()
    val watchLater = watchLaterDao.getAllWatchLater()
    val subscriptions = subscriptionDao.getAllSubscriptions()

    suspend fun addHistory(item: HistoryItem) = historyDao.insertHistory(item)
    suspend fun addWatchLater(item: WatchLaterItem) = watchLaterDao.insertWatchLater(item)
    suspend fun addSubscription(sub: SubscriptionEntity) = subscriptionDao.insertSubscription(sub)
}
