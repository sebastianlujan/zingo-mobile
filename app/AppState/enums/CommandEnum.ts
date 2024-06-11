export enum CommandEnum {
  sendprogress = 'sendprogress',
  changeserver = 'changeserver',
  wallet_kind = 'wallet_kind',
  interrupt_sync_after_batch = 'interrupt_sync_after_batch',
  updatecurrentprice = 'updatecurrentprice',
  notes = 'notes',
  summaries = 'summaries',
  height = 'height',
  setoption = 'setoption',
  getoption = 'getoption',
  info = 'info',
  version = 'version',
  export = 'export',
  new = 'new',
  import = 'import',
  exportufvk = 'exportufvk',
  sync = 'sync',
  syncstatus = 'syncstatus',
  addresses = 'addresses',
  parse_address = 'parse_address',
  parse_viewkey = 'parse_viewkey',
  balance = 'balance',
  seed = 'seed',
  rescan = 'rescan',
  value_to_address = 'value_to_address',
  sends_to_address = 'sends_to_address',
  memobytes_to_address = 'memobytes_to_address',

  // calculate the max soendable amount in the wallet
  spendablebalance = 'spendablebalance',
  // new commands to create a proposal
  send = 'send',
  sendall = 'sendall',
  shield = 'shield',
  // this works for all: send, sendall & shield
  confirm = 'confirm',
}
