import { n as e } from "./chunk-DiqZc92J.js";
import { _ as t, g as n } from "./icons-Daxdm7Mp.js";
import { l as r, o as i } from "./consts-PqkJ5Usg.js";
import { n as a, t as o } from "./section-panel-ui-state-ByeyD6ou.js";
import { n as s, r as c } from "./copilot-ui-state-DjlBBpyf.js";
import { r as l, t as u } from "./stats-D_j8yo6z.js";
import { n as d, t as f } from "./early-project-state-D-4_8bD-.js";
//#region frontend/copilot/shared/copilot-development-setup-user-guide-utils.ts
function p() {
	l("use-dev-workflow-guide"), a.openPanel(y);
}
function m() {
	let e = f.jdkInfo;
	return e ? e.jrebel ? "success" : e.hotswapAgentFound ? !e.hotswapVersionOk || !e.runningWithExtendClassDef || !e.runningWitHotswap || !e.runningInJavaDebugMode ? "error" : "success" : "warning" : null;
}
function h() {
	let e = f.jdkInfo;
	return !e || m() !== "success" ? "none" : e.jrebel ? "jrebel" : e.runningWitHotswap ? "hotswap" : "none";
}
function g() {
	return s.idePluginState !== void 0 && !s.idePluginState.active ? "warning" : "success";
}
function _() {
	if (!f.jdkInfo) return { status: "success" };
	let e = m(), t = g();
	return e === "warning" ? t === "warning" ? {
		status: "warning",
		message: "IDE Plugin, Hotswap"
	} : {
		status: "warning",
		message: "Hotswap is not enabled"
	} : t === "warning" ? {
		status: "warning",
		message: "IDE Plugin is not active"
	} : e === "error" ? {
		status: "error",
		message: "Hotswap is partially enabled"
	} : { status: "success" };
}
function v() {
	t(`${i}get-dev-setup-info`, {}), window.Vaadin.copilot.eventbus.on("copilot-get-dev-setup-info-response", (e) => {
		if (e.detail.content) {
			let t = JSON.parse(e.detail.content);
			s.setIdePluginState(t.ideInfo);
		}
	});
}
var y, b = e((() => {
	c(), n(), r(), o(), u(), d(), y = "copilot-development-setup-user-guide";
}));
//#endregion
export { g as a, p as c, m as i, v as n, h as o, _ as r, b as s, y as t };
