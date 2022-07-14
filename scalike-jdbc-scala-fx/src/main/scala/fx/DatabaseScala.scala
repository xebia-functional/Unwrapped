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
    fork { () =>
      try DB.readOnly(execution)
      catch case e: SQLException => e.shift
    }

  def localTransaction[A](execution: DBSession => A)(
      implicit context: CPContext = NoCPContext,
      boundary: TxBoundary[A] = TxBoundary.Exception.exceptionTxBoundary[A],
      settings: SettingsProvider = SettingsProvider.default): Transaction[A] =
    fork { () =>
      try DB.localTx(execution)
      catch case e: Exception => e.shift
    }
