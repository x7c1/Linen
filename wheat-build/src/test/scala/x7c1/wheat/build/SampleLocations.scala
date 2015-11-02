package x7c1.wheat.build

import java.io.File

import x7c1.wheat.build.layout.LayoutLocations
import x7c1.wheat.build.values.ValuesLocation

object SampleLocations {

  def packages = WheatPackages(
    starter = "x7c1.linen",
    starterLayout = "x7c1.linen.res.layout",
    glueLayout = "x7c1.linen.glue.res.layout",
    glueValues = "x7c1.linen.glue.res.values"
  )

  def directories = WheatDirectories(
    starter = new File("linen-starter"),
    glue = new File("linen-glue")
  )

  def layout = LayoutLocations(
    packages = packages,
    directories = directories
  )

  def values = ValuesLocation(
    packages = packages,
    directories = directories
  )
}
