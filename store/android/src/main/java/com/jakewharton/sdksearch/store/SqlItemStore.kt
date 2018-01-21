package com.jakewharton.sdksearch.store

import android.database.sqlite.SQLiteConstraintException
import com.squareup.sqlbrite3.BriteDatabase
import com.squareup.sqlbrite3.inTransaction
import com.squareup.sqldelight.SqlDelightCompiledStatement
import com.squareup.sqldelight.SqlDelightQuery
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SqlItemStore @Inject constructor(
    private val db: BriteDatabase
) : ItemStore {
  private val insertItem by lazy { ItemModel.Insert_item(db.writableDatabase) }
  private val updateItem by lazy { ItemModel.Update_item(db.writableDatabase) }
  private val queryTermMapper = SqlItem.FACTORY.query_termMapper()

  override suspend fun updateItems(items: List<Item>) {
    db.inTransaction {
      for (item in items) {
        try {
          insertItem.insert {
            bind(item.packageName, item.className, item.deprecated, item.link)
          }
        } catch (e: SQLiteConstraintException) {
          updateItem.update {
            bind(item.packageName, item.className, item.deprecated, item.link)
          }
        }
      }
    }
  }

  override fun queryItems(term: String) =
      db.createQuery(SqlItem.FACTORY.query_term(term.escapeLike('\\')))
          .mapToList { queryTermMapper.map(it).item }

  override fun count() = db.createQuery(SqlItem.FACTORY.count())
      .mapToOne(SqlItem.FACTORY.countMapper()::map)

  private fun <T : SqlDelightCompiledStatement> T.insert(binder: T.() -> Unit) {
    synchronized(this) {
      binder()
      db.executeInsert(table, program)
    }
  }

  private fun <T : SqlDelightCompiledStatement> T.update(binder: T.() -> Unit) {
    synchronized(this) {
      binder()
      db.executeUpdateDelete(table, program)
    }
  }
}

private fun String.escapeLike(escapeChar: Char) =
    this.replace("$escapeChar", "$escapeChar$escapeChar")
        .replace("%", "$escapeChar%")
        .replace("_", "${escapeChar}_")

private fun BriteDatabase.createQuery(query: SqlDelightQuery)
    = query.let { createQuery(it.tables, it) }
