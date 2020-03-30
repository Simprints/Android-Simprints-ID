### Component NFC
These component NFC classes are designed to wrap completely the Android SDK `android.nfc` package functionality.

`android` contains the classes for use in production that delegate to the real Android implementation.

The entry point is `ComponentNfcAdapter`, which should be provided in dependency injection framework.
In `java.main` code, include the production implementation for example:

```
open fun provideNfcAdapter(context: Context): ComponentNfcAdapter = AndroidNfcAdapter(context)
```

Which allows easily in testing contexts to swap the provide for simulated or mocked versions of the components:
```
// Assuming you have a SimulatedNfcManager class with which you configure the behaviour you want
override fun provideNfcAdapter(context: Context): ComponentNfcAdapter = SimulatedNfcAdapter(SimulatedNfcManager)
```

It's important to use the `Component...` version of all classes otherwise found in `android.nfc` throughout the code in order for clean swapping between different implementations.
