# Dummy Bluetooth Components

These classes can be used for insertion into tests for very basic use
allowing tests to compile. They can be used to tick the box of having a
`ComponentBluetoothAdapter` that is enabled, or if the MAC address of
the `ComponentBluetoothDevice` is needed. Attempts to connect or
communicate with these devices will throw an
`UnsupportedOperationException`.
