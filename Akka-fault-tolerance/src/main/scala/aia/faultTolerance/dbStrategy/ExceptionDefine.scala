package aia.faultTolerance.dbStrategy


object ExceptionDefine {

  class DbNodeDownException(msg: String) extends Exception(msg) with Serializable
  class ColumnNotFoundException(msg: String) extends Exception(msg) with Serializable
  class DbBrokenConnectionException(msg: String) extends Exception(msg) with Serializable
  class DiskError extends Error with Serializable
}
