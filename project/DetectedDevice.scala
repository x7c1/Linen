import InstallerError.{DeviceNotConnected, UnknownFormatLine}
import x7c1.wheat.splicer.lib.Extractor
import x7c1.wheat.splicer.lib.Extractor.==>


case class DetectedDevice(deviceId: String)

object DetectedDevice {

  def from(lines: Seq[String]): Either[InstallerError, DetectedDevice] =
    lines filter (_.nonEmpty) match {
      case _ +: detect(device) +: _ =>
        Right(device)
      case _ +: line +: _ =>
        Left(UnknownFormatLine(line))
      case _ =>
        Left(DeviceNotConnected())
    }

  private val detect: String ==> DetectedDevice = Extractor {
    _.split("\t").toSeq match {
      case device +: _ if device.nonEmpty =>
        Some(DetectedDevice(device))
      case _ =>
        None
    }
  }

}
