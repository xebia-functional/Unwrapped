package fx

import scalikejdbc.*
import scalikejdbc.DB.{CPContext, NoCPContext, readOnly}

import java.sql.SQLException
import fx.ensure

type Database[A] = (Structured, Control[SQLException]) ?=> Fiber[A]

extension (db: DB.type)
  def readOnlyWithControl[A](execution: DBSession => A)(
      using context: CPContext = NoCPContext,
      settings: SettingsProvider = SettingsProvider.default): Database[A] =
    fork { () =>
      try DB.readOnly(execution)
      catch case e: SQLException => e.shift
    }
