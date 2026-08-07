import { n as e } from "./chunk-DiqZc92J.js";
import { n as t, t as n } from "./copilot-eventbus-tR06exIW.js";
import { n as r, r as i } from "./copilot-ui-state-DjlBBpyf.js";
import { n as a, t as o } from "./copilot-stored-machine-state-DS3t0BPl.js";
//#region frontend/copilot/shared/copilot-notification.ts
function s(e) {
	r.notifications.includes(e) && (e.dontShowAgain && e.dismissId && l(e.dismissId), r.removeNotification(e), n.emit("notification-dismissed", e));
}
function c(e) {
	return a.getDismissedNotifications().includes(e);
}
function l(e) {
	c(e) || a.addDismissedNotification(e);
}
function u(e) {
	return !(e.dismissId && (c(e.dismissId) || r.notifications.find((t) => t.dismissId === e.dismissId)));
}
function d(e) {
	if (u(e)) return f(e);
}
function f(e) {
	let t = m;
	m += 1;
	let i = {
		...e,
		id: t,
		dontShowAgain: !1,
		animatingIn: !0,
		animatingOut: !1
	};
	return r.setNotifications([...r.notifications, i]), requestAnimationFrame(() => {
		i.animatingIn = !1, r.setNotifications([...r.notifications]);
	}), (e.delay || !e.link && !e.dismissId) && setTimeout(() => {
		s(i);
	}, e.delay ?? p), n.emit("notification-shown", e), i;
}
var p, m, h = e((() => {
	t(), o(), i(), p = 5e3, m = 1;
}));
//#endregion
export { h as n, d as r, s as t };
