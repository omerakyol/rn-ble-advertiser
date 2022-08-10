import React, { useEffect } from 'react';

import { StyleSheet, View, Text } from 'react-native';
import ReactNativeBleAdvertiser from 'tp-rn-ble-advertiser';

export default function App() {
  const [result, setResult] = React.useState<string>();

  useEffect(() => {
    ReactNativeBleAdvertiser.startBroadcast('Hellooo!!! Hear Meeeee !!!!');
    setResult('Broadcast started');

    setTimeout(() => {
      ReactNativeBleAdvertiser.stopBroadcast();
      setResult('Broadcast stopped');
    }, 10000);
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
