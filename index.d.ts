
export interface IElepayConfig {
    publicKey?: string;
    apiUrl?: string;
    googlePayEnvironment?: string;
    languageKey?: Language_Key;
}

export type Language_Key = 'english' | 'simplifiedchinise' | 'traditionalchinese' | 'japanese' | ''

export type Elepay_State = 'succeeded' | 'cancelled' | 'failed'


export interface IElepayResult {
    state: Elepay_State
    paymentId: null | string
}


export interface IElepayError {
    code: string
    reason: string
    message: string
}

export type ElepayCallBack = (result: IElepayResult, error?: IElepayError) => void

export function initElepay(config: IElepayConfig): void
export function handlePaymentWithPayload(payload: string, callback: ElepayCallBack): void
export function changeLanguage(value:{ languageKey?: LANGUAGE_KEY } = { languageKey: ''}): void
export function handleOpenUrlString(url: string): void

