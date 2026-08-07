import { n as e } from "./chunk-DiqZc92J.js";
import { _ as t, g as n } from "./icons-Daxdm7Mp.js";
//#region frontend/copilot/shared/stats.ts
var r, i, a, o, s = e((() => {
	n(), r = () => {
		t("copilot-browser-info", {
			userAgent: navigator.userAgent,
			locale: navigator.language,
			timezone: Intl.DateTimeFormat().resolvedOptions().timeZone
		});
	}, i = (e, n) => {
		t("copilot-track-event", {
			event: e,
			properties: n
		});
	}, a = (e, t) => {
		i(e, {
			...t,
			view: "react"
		});
	}, o = (e, t) => {
		i(e, {
			...t,
			view: "flow"
		});
	};
}));
//#endregion
export { a, o as i, r as n, i as r, s as t };
