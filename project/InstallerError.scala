
sealed trait InstallerError {
  def message: String
}

object InstallerError {

  case class DeviceNotConnected() extends InstallerError {
    override def message = "device not connected"
  }

  case class UnknownFormatLine(line: String) extends InstallerError {
    override def message = s"unknown format line: $line"
  }

}
