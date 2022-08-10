import { NativeModules } from 'react-native';

type ReactNativeBleAdvertiserType = {
  startBroadcast(data: string): void;
  stopBroadcast(): void;
};

const { ReactNativeBleAdvertiser } = NativeModules;

export default ReactNativeBleAdvertiser as ReactNativeBleAdvertiserType;
