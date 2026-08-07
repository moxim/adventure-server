import { n as e } from "./chunk-DiqZc92J.js";
import { a as t, c as n, d as r, n as i, o as a, t as o } from "./dom-utils-U1Di7XbE.js";
import { n as s, t as c } from "./copilot-eventbus-tR06exIW.js";
import { i as l, n as u } from "./copilot-modes-BbD7YWYd.js";
import { n as d, r as f } from "./copilot-ui-state-DjlBBpyf.js";
import { n as p, t as m } from "./copilot-stored-machine-state-DS3t0BPl.js";
import { n as h, t as g } from "./track-active-mode-event-zQOkAKHr.js";
//#region frontend/copilot/shared/os-utils.ts
function _() {
	let e = window.navigator.userAgent;
	return e.indexOf("Windows") === -1 ? e.indexOf("Mac") === -1 ? e.indexOf("Linux") === -1 ? null : "Linux" : "Mac" : "Windows";
}
function v() {
	return _() === "Mac";
}
function y() {
	return v() ? "⌘" : "Ctrl";
}
var b = e((() => {}));
//#endregion
//#region frontend/copilot/copilot-shortcuts.ts
function x(e) {
	if ((e.ctrlKey || e.metaKey) && e.key === "c" && !e.shiftKey) {
		let e = document.querySelector("copilot-main")?.shadowRoot, t;
		if (t = typeof e?.getSelection == "function" ? e?.getSelection() : document.getSelection() ?? void 0, t && t.rangeCount === 1) {
			let e = t.getRangeAt(0).commonAncestorContainer;
			if (e.nodeType === Node.TEXT_NODE) return r(e);
		}
	}
	return !1;
}
function S(e) {
	let t = i(e, "vaadin-context-menu-overlay");
	if (!t) return !1;
	let n = t.owner;
	return n ? !!i(n, "copilot-component-overlay") : !1;
}
function C() {
	return d.idePluginState?.supportedActions?.find((e) => e === "undo");
}
function w(e) {
	let t = e;
	if (o(e)) return !0;
	let n = a(t);
	for (let e of n) if (o(e)) return !0;
	return !1;
}
var T, E, D, O, k, A, j, M, N, P, F = e((() => {
	s(), f(), b(), m(), n(), l(), g(), T = !1, E = 0, D = (e) => {
		if (p.isActivationShortcut() && p.getToolbarExpandMode() !== "never") if (e.key === "Shift" && !e.ctrlKey && !e.altKey && !e.metaKey) T = !0;
		else if (T && e.shiftKey && (e.key === "Control" || e.key === "Meta")) {
			if (E++, E === 2) return d.activeMode === "play" ? d.lastNonPlayMode === void 0 ? d.setActiveMode("edit", !0) : d.setActiveMode(d.lastNonPlayMode, !0) : d.setActiveMode("play", !0), h(), E = 0, !0;
			setTimeout(() => {
				E = 0;
			}, 500);
		} else E = 0;
		return !1;
	}, O = (e) => {
		if (D(e)) {
			e.stopPropagation();
			return;
		}
		if (u()?.appInteractable) return;
		let n = t();
		if (!n) return;
		let r = S(n), a = i(n, "vaadin-dialog") ?? (n.localName === "vaadin-dialog" ? n : null), o = a !== null && a.hasAttribute("panel-container"), s = n.localName === "copilot-main", l = i(n, "copilot-outline-panel") !== null, f = i(n, "copilot-toolbar") !== null;
		if (!s && !r && e.key !== "Escape" && !l && !f) {
			e.stopPropagation();
			return;
		}
		let p = !0, m = !1;
		if (x(e)) p = !1;
		else if (e.key === "Escape") {
			if (d.loginCheckActive && d.setLoginCheckActive(!1), w(n)) {
				e.stopPropagation();
				return;
			}
			c.emit("escape-key-pressed", { event: e });
		} else j(e) && d.activeMode === "edit" && (!o || l) ? (c.emit("delete-selected", {}), m = !0) : (e.ctrlKey || e.metaKey) && e.key === "d" && d.activeMode === "edit" && (!o || l) ? (c.emit("duplicate-selected", {}), m = !0) : (e.ctrlKey || e.metaKey) && e.key === "b" && (!o || l) ? (c.emit("show-selected-in-ide", { attach: e.shiftKey }), m = !0) : (e.ctrlKey || e.metaKey) && e.key === "z" && C() && (!o || l) ? (c.emit("undoRedo", { undo: !e.shiftKey }), m = !0) : x(e) || c.emit("keyboard-event", { event: e });
		d.setMultiSelectionOn(A(e)), p && e.stopPropagation(), m && e.preventDefault();
	}, k = (e) => {
		if (u()?.appInteractable) return;
		let n = t();
		n && n.localName === "copilot-main" && A(e) && d.setMultiSelectionOn(!1);
	}, A = (e) => (e.key === "Control" || e.key === "Meta") && !e.shiftKey && !e.altKey, j = (e) => (e.key === "Backspace" || e.key === "Delete") && !e.shiftKey && !e.ctrlKey && !e.altKey && !e.metaKey, M = y(), N = "⇧", P = {
		toggleCopilot: `<kbd>${N} + ${M} ${M}</kbd>`,
		openAiPopover: `<kbd>${N} + Space</kbd>`,
		undo: `<kbd>${M} + Z</kbd>`,
		redo: `<kbd>${M} + ${N} + Z</kbd>`,
		duplicate: `<kbd>${M} + D</kbd>`,
		goToSource: `<kbd>${M} + B</kbd>`,
		goToAttachSource: `<kbd>${M} + ${N} + B</kbd>`,
		selectParent: "<kbd>←</kbd>",
		selectPreviousSibling: "<kbd>↑</kbd>",
		selectNextSibling: "<kbd>↓</kbd>",
		delete: "<kbd>DEL</kbd>",
		copy: `<kbd>${M} + C</kbd>`,
		paste: `<kbd>${M} + V</kbd>`
	};
}));
//#endregion
export { P as a, F as i, k as n, O as r, D as t };
