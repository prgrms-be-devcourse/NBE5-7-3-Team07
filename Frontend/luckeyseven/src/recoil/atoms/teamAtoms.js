import {atom} from 'recoil';
import {recoilPersist} from "recoil-persist";

const {persistAtom} = recoilPersist();

export const currentTeamIdState = atom({
  key: "currentTeamIdState",
  default: null, // 최초엔 null, 추후 URL로부터 세팅
  effects_UNSTABLE: [persistAtom], // persistAtom은 recoil-persist에서 제공하는 함수로, atom의 상태를 localStorage에 저장합니다.
});

// 팀의 외화 통화 단위를 저장하는 atom
export const teamForeignCurrencyState = atom({
  key: "teamForeignCurrencyState",
  default: "외화", // 기본값은 USD
  effects_UNSTABLE: [persistAtom],
});