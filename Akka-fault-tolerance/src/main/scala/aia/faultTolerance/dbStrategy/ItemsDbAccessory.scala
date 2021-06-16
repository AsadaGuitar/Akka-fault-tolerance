package aia.faultTolerance.dbStrategy

import aia.faultTolerance.dbStrategy.ExceptionDefine.DbBrokenConnectionException

import java.sql.{Connection, DriverManager, PreparedStatement}

class ItemsDbAccessory(val url: String) {

  val driver: String = "com.mysql.cj.jdbc.Driver"
  val username: String = "root"
  val password: String = "Asd18894"
  var connection: Option[Connection] = None

  def setConnection(): Unit ={
    try {
      Class.forName (driver)
      connection = Some(DriverManager.getConnection (url, username, password))
    }
    catch {case _: Exception => throw new DbBrokenConnectionException("Fail Connect Database.")}
  }

  def create(item: String): Unit ={
    setConnection()
    val sql = s"insert into akka (data) values(\"$item\")"
    val ps: PreparedStatement = connection.get.prepareStatement(sql)
    ps.executeUpdate()
  }

  def close(): Unit = connection.get.close()
}