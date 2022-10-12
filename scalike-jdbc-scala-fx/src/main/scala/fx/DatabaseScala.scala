package fx

import scalikejdbc.*
import scalikejdbc.DB.{readOnly, CPContext, NoCPContext}

import java.sql.SQLException

type Database[A] = Raise[SQLException] ?=> A
type Transaction[A] = Raise[Exception] ?=> A

extension (db: DB.type)
  def readOnlyWithControl[A](execution: DBSession => A)(
      using context: CPContext = NoCPContext,
      settings: SettingsProvider = SettingsProvider.default): Database[A] =
    handle(DB.readOnly(execution))((e: SQLException) => e.raise)

  def localTransaction[A](execution: DBSession => A)(
      using context: CPContext = NoCPContext,
      boundary: TxBoundary[A] = TxBoundary.Exception.exceptionTxBoundary[A],
      settings: SettingsProvider = SettingsProvider.default): Transaction[A] =
    handle(DB.localTx(execution))((e: Exception) => e.raise)
