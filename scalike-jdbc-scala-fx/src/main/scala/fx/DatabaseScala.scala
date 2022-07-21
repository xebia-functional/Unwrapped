package fx

import scalikejdbc.*
import scalikejdbc.DB.{readOnly, CPContext, NoCPContext}

import java.sql.SQLException
import fx.ensure

type Database[A] = (Structured, Control[SQLException]) ?=> Fiber[A]
type Transaction[A] = (Structured, Control[Exception]) ?=> Fiber[A]

extension (db: DB.type)
  def readOnlyWithControl[A](execution: DBSession => A)(
      using context: CPContext = NoCPContext,
      settings: SettingsProvider = SettingsProvider.default): Database[A] =
    fork { () => handle(DB.readOnly(execution))((e: SQLException) => e.shift) }

  def localTransaction[A](execution: DBSession => A)(
      using context: CPContext = NoCPContext,
      boundary: TxBoundary[A] = TxBoundary.Exception.exceptionTxBoundary[A],
      settings: SettingsProvider = SettingsProvider.default): Transaction[A] =
    fork { () => handle(DB.localTx(execution))((e: Exception) => e.shift) }
