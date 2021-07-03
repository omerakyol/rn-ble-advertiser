import { NativeModules } from 'react-native';

type ReactNativeBleAdvertiserType = {
  init(): void;
  setData(data: string): void;

  startBroadcast(): void;
  stopBroadcast(): void;
};

const { ReactNativeBleAdvertiser } = NativeModules;

export default ReactNativeBleAdvertiser as ReactNativeBleAdvertiserType;
