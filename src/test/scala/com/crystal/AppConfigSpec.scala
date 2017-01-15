package com.crystal
import org.scalatest._
import com.typesafe.config.ConfigFactory

class AppConfigSpec extends FlatSpec with Matchers with BeforeAndAfterEach {
  override def afterEach() = {
    // Reset CLI Args
    AppConfig.setArgs(Array())
  }

  it should "return config when no args have been given" in {
    AppConfig.load() should not be empty
  }

  it should "default to ConfigFactory if no args given" in {
    val config = ConfigFactory.load()

    AppConfig.load().get.streamName should equal (config.getString("stream_name"))
    AppConfig.load().get.appName should equal (config.getString("app_name"))
    AppConfig.load().get.regionName should equal (config.getString("region_name"))
    AppConfig.load().get.checkpointInterval should equal (config.getInt("checkpoint_interval"))
    AppConfig.load().get.userTable should equal (config.getString("user_table"))
    AppConfig.load().get.userIdentifier should equal (config.getString("user_identifier"))
  }

  it should "respect the --streamName and -s CLI args" in {
    AppConfig.setArgs(Array("--streamName", "testName"))
    AppConfig.load().get.streamName should equal ("testName")

    AppConfig.setArgs(Array("-s", "testName2"))
    AppConfig.load().get.streamName should equal ("testName2")
  }

  it should "respect the --outStreamName and -o CLI args" in {
    AppConfig.setArgs(Array("--outStreamName", "testName"))
    AppConfig.load().get.outStreamName should equal ("testName")

    AppConfig.setArgs(Array("-o", "testName2"))
    AppConfig.load().get.outStreamName should equal ("testName2")
  }

  it should "respect the --appName CLI arg" in {
    AppConfig.setArgs(Array("--appName", "testApp"))
    AppConfig.load().get.appName should equal ("testApp")
  }

  it should "respect the --regionName and -r CLI args" in {
    AppConfig.setArgs(Array("--regionName", "testRegion"))
    AppConfig.load().get.regionName should equal ("testRegion")

    AppConfig.setArgs(Array("-r", "testRegion2"))
    AppConfig.load().get.regionName should equal ("testRegion2")
  }

  it should "respect the --checkpointInterval and -c CLI args" in {
    AppConfig.setArgs(Array("--checkpointInterval", "123"))
    AppConfig.load().get.checkpointInterval should equal (123)

    AppConfig.setArgs(Array("-c", "1234"))
    AppConfig.load().get.checkpointInterval should equal (1234)
  }

  it should "respect the --userTable CLI arg" in {
    AppConfig.setArgs(Array("--userTable", "users_seg_test"))
    AppConfig.load().get.userTable should equal ("users_seg_test")
  }

  it should "respect the --userIdentifier CLI arg" in {
    AppConfig.setArgs(Array("--userIdentifier", "domain_userid_test"))
    AppConfig.load().get.userIdentifier should equal ("domain_userid_test")
  }
}
