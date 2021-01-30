prebuilt_jar(
  name = 'junit',
  binary_jar = 'lib/junit.jar',
  visibility = [ 'PUBLIC' ],
)

prebuilt_jar(
  name = 'hamcrest',
  binary_jar = 'lib/hamcrest-core-1.3.jar',
  visibility = [ 'PUBLIC' ],
)

prebuilt_jar(
  name = 'jackson',
  binary_jar = 'lib/jackson-core-2.9.7.jar',
  source_jar = 'lib/jackson-core-2.9.7-sources.jar',
  visibility = [ 'PUBLIC' ],
)

prebuilt_jar(
  name = 'jmock',
  binary_jar = 'lib/jmock-2.12.0.jar',
  source_jar = 'lib/jmock-2.12.0-sources.jar',
  visibility = [ 'PUBLIC' ],
)

prebuilt_jar(
  name = 'jmock-junit4',
  binary_jar = 'lib/jmock-junit4-2.12.0.jar',
  source_jar = 'lib/jmock-junit4-2.12.0-sources.jar',
  visibility = [ 'PUBLIC' ],
)

prebuilt_jar(
  name = 'netty-buffer',
  binary_jar = 'lib/netty-buffer-4.1.56.Final.jar',
  source_jar = 'lib/netty-buffer-4.1.56.Final-sources.jar',
  visibility = [ 'PUBLIC' ],
  deps = [
      ':netty-common'
  ]
)

prebuilt_jar(
  name = 'netty-codec',
  binary_jar = 'lib/netty-codec-4.1.56.Final.jar',
  source_jar = 'lib/netty-codec-4.1.56.Final-sources.jar',
  visibility = [ 'PUBLIC' ],
  deps = [
    ':netty-common'
  ]
)

prebuilt_jar(
  name = 'netty-resolver',
  binary_jar = 'lib/netty-resolver-4.1.56.Final.jar',
  source_jar = 'lib/netty-resolver-4.1.56.Final-sources.jar',
  visibility = [ 'PUBLIC' ],
  deps = [
    ':netty-common'
  ]
)

prebuilt_jar(
  name = 'netty-transport',
  binary_jar = 'lib/netty-transport-4.1.56.Final.jar',
  source_jar = 'lib/netty-transport-4.1.56.Final-sources.jar',
  visibility = [ 'PUBLIC' ],
  deps = [
    ':netty-common'
  ]
)

prebuilt_jar(
  name = 'netty-common',
  binary_jar = 'lib/netty-common-4.1.56.Final.jar',
  source_jar = 'lib/netty-common-4.1.56.Final-sources.jar',
  visibility = [ 'PUBLIC' ],
  deps = []
)

keystore(
  name = 'keystore',
  store = 'temporary.keystore',
  properties = 'keystore.properties',
  visibility = [ 'PUBLIC' ],
)