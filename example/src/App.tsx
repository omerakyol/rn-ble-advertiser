import React, { useEffect } from 'react';

import { StyleSheet, View, Text } from 'react-native';
import ReactNativeBleAdvertiser from 'tp-rn-ble-advertiser';

export default function App() {
  const [result, setResult] = React.useState<string>();

  useEffect(() => {
    ReactNativeBleAdvertiser.initializeBle();
    ReactNativeBleAdvertiser.setData('Hellooo!!! Hear Meeeee !!!!');

    setTimeout(() => {
      ReactNativeBleAdvertiser.startBroadcast();
      setResult('Broadcast started');
    }, 500);

    // setTimeout(() => {
    //   ReactNativeBleAdvertiser.stopBroadcast();
    //   setResult('Broadcast stopped');
    // }, 15000);
    setResult('Initialized');
  }, []);

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
