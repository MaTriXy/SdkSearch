package com.jakewharton.sdksearch.store

actual interface ItemStore {
  actual suspend fun updateItems(items: List<Item>)
}
