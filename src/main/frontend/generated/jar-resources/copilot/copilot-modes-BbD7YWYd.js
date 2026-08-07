import { i as e, n as t } from "./chunk-DiqZc92J.js";
import { _ as n, g as r } from "./icons-Daxdm7Mp.js";
import { l as i, n as a, s as o } from "./consts-PqkJ5Usg.js";
import { n as s, t as c } from "./section-panel-ui-state-ByeyD6ou.js";
import { n as l, r as u } from "./copilot-ui-state-DjlBBpyf.js";
//#region frontend/copilot/plugins/copilot-plugins.ts
function d(e) {
	e.init({
		addPanel: (e) => {
			s.addPanel(e);
		},
		send(e, t) {
			n(e, t);
		}
	});
}
function f() {
	h().publicPluginsState === "NOT_INITIALIZED" && (g.push(import("./copilot-log-plugin-B_UbCOeB.js")), g.push(import("./copilot-info-plugin-Ds2Kp49I.js")), g.push(import("./copilot-features-plugin-z3x_bwos.js")), g.push(import("./copilot-feedback-plugin-DbIrhuZj.js")), g.push(import("./copilot-settings-panel-BBHMoj_D.js")), g.push(import("./copilot-impersonator-plugin-C0UGEOJG.js")), g.push(import("./copilot-development-setup-user-guide-1hsMunAb.js")), g.push(import("./copilot-vaadin-versions-CkKpdgBo.js")), v = !0, h().setPublicPluginsState("IMPORTED"));
}
function p() {
	if (h().privatePluginsState === "NOT_INITIALIZED") {
		let e = window.Vaadin?.copilot?._localPluginsUrl, t = `https://cdn.vaadin.com/copilot/${o}/copilot-plugins${a}.js`, n = e || t;
		console.debug(`Loading private plugins from: ${n}`), import(
			/* @vite-ignore */
			n
).then(() => {
			h().setPrivatePluginsState("INITIALIZED");
		}).catch((e) => {
			console.warn(`Unable to load plugins from ${n}. Some Copilot features are unavailable.`, e);
		});
	}
}
function m() {
	Promise.all(g).then(() => {
		let e = window.Vaadin;
		if (e.copilot.plugins) {
			let t = e.copilot.plugins;
			e.copilot.plugins.push = (e) => d(e), Array.from(t).forEach((e) => {
				_.includes(e) || (d(e), _.push(e));
			});
		}
	}), g = [], v && h().setPublicPluginsState("INITIALIZED");
}
function h() {
	return window.Vaadin.copilot._uiState;
}
var g, _, v, y = t((() => {
	c(), i(), r(), g = [], _ = [], v = !1;
})), b, x = t((() => {
	b = window.Vaadin.copilot.overlayManager;
}));
//#endregion
//#region frontend/copilot/dynamic-module-loader.ts
async function S() {
	return (await import("./copilot-focus-trap-CprauOBI.js")).copilotFocusTrap;
}
function C() {
	return import("./typescript-rXzk1P2l.js").then((t) => /* @__PURE__ */ e(t.default, 1));
}
var w = t((() => {}));
//#endregion
//#region frontend/copilot/shared/copilot-modes.ts
function T() {
	b.addOverlayOutsideClickEvent(), b.activate();
}
function E() {
	b.removeOverlayOutsideClickEvent(), b.deactivate();
}
function D() {
	let e = k[l.activeMode];
	return l.forceSelectionEnabled ? {
		...e,
		appInteractable: !1
	} : e;
}
function O(e) {
	return k[e];
}
var k, A = t((() => {
	u(), y(), w(), x(), k = {
		edit: {
			label: "Edit",
			appInteractable: !1,
			toolbarIcon: "code",
			toolbarOrder: 0,
			onActivation: async () => {
				let e = await S();
				e.active || e.activate(), T(), p();
			}
		},
		test: {
			label: "Test",
			appInteractable: !0,
			toolbarIcon: "bugReport",
			toolbarOrder: 1,
			onActivation: async (e) => {
				(e === "edit" || e === "inspect") && ((await S()).deactivate(), E()), p();
			}
		},
		inspect: {
			label: "Inspect",
			appInteractable: !1,
			toolbarIcon: "visibility",
			toolbarOrder: 2,
			onActivation: async () => {
				let e = await S();
				e.active || e.activate(), T(), p();
			}
		},
		play: {
			label: "Play",
			appInteractable: !0,
			toolbarIcon: "playCircle",
			toolbarOrder: 3,
			default: !0,
			onActivation: async (e) => {
				(e === "edit" || e === "inspect") && ((await S()).deactivate(), E());
			}
		}
	};
}));
//#endregion
export { C as a, x as c, m as d, A as i, f as l, D as n, w as o, O as r, b as s, k as t, y as u };
