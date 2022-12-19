/**
 * @format
 */

import 'react-native';
import React from 'react';

import { render } from '@testing-library/react-native';
import PrivKey from '../components/PrivKey';
import { ContextLoadedProvider } from '../app/context';

import {
  ErrorModalData,
  InfoType,
  ReceivePageState,
  SendPageState,
  SendProgress,
  SyncStatusReport,
  ToAddr,
  TotalBalance,
  WalletSettings,
} from '../app/AppState';

jest.useFakeTimers();
jest.mock('@fortawesome/react-native-fontawesome', () => ({
  FontAwesomeIcon: '',
}));
jest.mock('react-native-localize', () => ({
  getNumberFormatSettings: () => {
    return {
      decimalSeparator: '.',
      groupingSeparator: ',',
    };
  },
}));
jest.mock('react-native/Libraries/Animated/NativeAnimatedHelper');

// test suite
describe('Component PrivKey - test', () => {
  //snapshot test
  const state = {
    navigation: null,
    route: null,
    dimensions: {} as {
      width: number;
      height: number;
      orientation: 'portrait' | 'landscape';
      deviceType: 'tablet' | 'phone';
      scale: number;
    },

    syncStatusReport: new SyncStatusReport(),
    addressPrivateKeys: new Map(),
    addresses: [],
    addressBook: [],
    transactions: null,
    sendPageState: new SendPageState(new ToAddr(0)),
    receivePageState: new ReceivePageState(),
    info: {} as InfoType,
    rescanning: false,
    wallet_settings: new WalletSettings(),
    syncingStatus: null,
    errorModalData: new ErrorModalData(),
    txBuildProgress: new SendProgress(),
    walletSeed: null,
    isMenuDrawerOpen: false,
    selectedMenuDrawerItem: '',
    aboutModalVisible: false,
    computingModalVisible: false,
    settingsModalVisible: false,
    infoModalVisible: false,
    rescanModalVisible: false,
    seedViewModalVisible: false,
    seedChangeModalVisible: false,
    seedBackupModalVisible: false,
    seedServerModalVisible: false,
    syncReportModalVisible: false,
    poolsModalVisible: false,
    newServer: null,
    uaAddress: null,
    translate: () => 'text translated',
    totalBalance: new TotalBalance(),
  };
  state.info.currencyName = 'ZEC';
  state.totalBalance.total = 1.12345678;
  const onClose = jest.fn();
  test('PrivKey Private - snapshot', () => {
    const privKey = render(
      <ContextLoadedProvider value={state}>
        <PrivKey
          address={'UA-12345678901234567890'}
          keyType={0}
          privKey={'priv-key-12345678901234567890'}
          closeModal={onClose}
        />
      </ContextLoadedProvider>,
    );
    expect(privKey.toJSON()).toMatchSnapshot();
  });
  test('PrivKey View - snapshot', () => {
    const privKey = render(
      <ContextLoadedProvider value={state}>
        <PrivKey
          address={'UA-12345678901234567890'}
          keyType={1}
          privKey={'view-key-12345678901234567890'}
          closeModal={onClose}
        />
      </ContextLoadedProvider>,
    );
    expect(privKey.toJSON()).toMatchSnapshot();
  });
});
