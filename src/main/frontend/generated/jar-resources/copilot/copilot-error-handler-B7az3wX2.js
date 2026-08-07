import { n as e } from "./chunk-DiqZc92J.js";
import { $ as t, C as n, L as r, R as i, T as a, at as o, et as s, l as c, lt as l, n as u, o as d, r as f, s as ee, t as p, u as m, y as h } from "./icons-Daxdm7Mp.js";
import { l as te, o as g } from "./consts-PqkJ5Usg.js";
import { n as _, t as v } from "./copilot-eventbus-tR06exIW.js";
import { n as y, r as b } from "./copilot-ui-state-DjlBBpyf.js";
import { t as ne } from "./stats-D_j8yo6z.js";
import { i as x, n as S, r as C, t as w } from "./directive-CZ105jp5.js";
import { n as T, t as E } from "./copilot-stored-machine-state-DS3t0BPl.js";
import { n as D } from "./copilot-notification-BnxWYvla.js";
import { n as O } from "./early-project-state-D-4_8bD-.js";
//#region node_modules/lit-html/directives/unsafe-html.js
var k, A, j = e((() => {
	c(), C(), k = class extends S {
		constructor(e) {
			if (super(e), this.it = d, e.type !== x.CHILD) throw Error(this.constructor.directiveName + "() can only be used in child bindings");
		}
		render(e) {
			if (e === d || e == null) return this._t = void 0, this.it = e;
			if (e === ee) return e;
			if (typeof e != "string") throw Error(this.constructor.directiveName + "() called with a non-string value");
			if (e === this.it) return this._t;
			this.it = e;
			let t = [e];
			return t.raw = t, this._t = {
				_$litType$: this.constructor.resultType,
				strings: t,
				values: []
			};
		}
	}, k.directiveName = "unsafeHTML", k.resultType = 1, A = w(k);
})), M = e((() => {
	j();
}));
//#endregion
//#region frontend/copilot/shared/copilot-userinfo-util.ts
function re() {
	let e = y.userInfo;
	return !e || e.copilotProjectCannotLeaveLocalhost ? !1 : T.isSendErrorReportsAllowed();
}
var ie = e((() => {
	ne(), b(), $(), E(), n(), D(), s();
}));
//#endregion
//#region frontend/copilot/shared/hotswap-utils.ts
function N() {
	return y.idePluginState?.supportedActions?.find((e) => e === "restartApplication");
}
function P() {
	i(`${g}plugin-restart-application`, {}, () => {}).catch((e) => {
		z("Error restarting server", e);
	});
}
var F = e((() => {
	r(), te(), $(), _(), D(), s(), b(), O();
}));
//#endregion
//#region frontend/copilot/shared/copilot-error-handler.ts
function I(e) {
	if (e === void 0) return !1;
	let t = Object.keys(e);
	return t.length === 1 && t.includes("message") || t.length >= 3 && t.includes("message") && t.includes("exceptionMessage") && t.includes("exceptionStacktrace");
}
function L() {
	let e = "A server restart is required";
	return N() ? h(m`${e}${R()}`) : h(m`${e}`);
}
function R() {
	return N() ? m`<vaadin-button
      class="mt-2"
      theme="primary"
      @click=${(e) => {
		let t = e.target;
		t.disabled = !0, t.innerText = "Restarting...", P();
	}}>
      Restart Now
    </vaadin-button>` : d;
}
function z(e, n) {
	let r = I(n) ? n.exceptionMessage ?? n.message : n, i = {
		type: t.ERROR,
		message: "Copilot internal error",
		details: e + (r ? `\n${r}` : "")
	};
	I(n) && n.suggestRestart && N() && (i.details = h(m`${e}<br />${r} ${R()}`), i.delay = 3e4), a(i);
	let o;
	o = n instanceof Error ? n.stack : I(n) ? n?.exceptionStacktrace?.join("\n") : n?.toString(), v.emit("system-info-with-callback", {
		callback: (t) => v.send("copilot-error", {
			message: `Copilot internal error: ${e}`,
			details: o,
			versions: t
		}),
		notify: !1
	});
}
function B(e) {
	return e?.stack?.includes("cdn.vaadin.com/copilot") || e?.stack?.includes("/copilot/copilot/") || e?.stack?.includes("/copilot/copilot-private/");
}
function V() {
	let e = window.onerror;
	window.onerror = (t, n, r, i, a) => {
		if (B(a)) {
			z(t.toString(), a);
			return;
		}
		e && e(t, n, r, i, a);
	}, l((e) => {
		B(e) && z("", e);
	});
	let t = window.Vaadin.ConsoleErrors;
	if (Array.isArray(t)) for (let e of t) Array.isArray(e) ? Q.push(...e) : Q.push(e);
	U((e) => Q.push(e));
}
function H(e, t, n, r, i, a) {
	let o = { ...e }, s = window.Vaadin.copilot.tree, c = window.Vaadin.copilot.customComponentHandler;
	o.nodes.forEach((e) => {
		e.node = s.allNodesFlat.find((t) => {
			if (!t.isFlowComponent) return !1;
			let n = t.node;
			return n.uiId === e.uiId && n.nodeId === e.nodeId;
		});
	});
	let l = [];
	n && l.push(`Error Message -> ${n}`), r && l.push(`Error Details -> ${r}`), l.push(`Active Level -> ${c.getActiveDrillDownContext() ? c.getActiveDrillDownContext()?.nameAndIdentifier : "No active level"}`), o.nodes.length > 0 && (l.push("\nRelevant Nodes:"), o.nodes.forEach((e) => {
		l.push(`${e.relevance} -> ${e.node?.nameAndIdentifier ?? "Node not found"}`);
	})), o.relevantPairs.length > 0 && (l.push("\nAdditional Info:"), o.relevantPairs.forEach((e) => {
		l.push(`${e.relevance} -> ${e.value}`);
	})), a && (l.push("Versions"), l.push(a));
	let u = {
		name: "Info",
		content: l.join("\n")
	};
	o.items.unshift(u), i && o.items.push({
		name: "Stacktrace",
		content: i
	}), v.emit("system-info-with-callback", {
		callback: (e) => {
			o.items.push({
				name: "Versions",
				content: e
			}), t(o);
		},
		notify: !1
	});
}
function U(e) {
	let n = window.Vaadin.ConsoleErrors;
	window.Vaadin.ConsoleErrors = { push: (r) => {
		r[0] === null || r[0] === void 0 || (r[0].type !== void 0 && r[0].message !== void 0 ? e({
			type: r[0].type,
			message: r[0].message,
			internal: !!r[0].internal,
			details: r[0].details,
			link: r[0].link
		}) : e({
			type: t.ERROR,
			message: r.map((e) => W(e)).join(" "),
			internal: !1
		}), n.push(r));
	} };
}
function W(e) {
	return e.message ? e.message.toString() : e.toString();
}
var G, K, q, J, Y, X, Z, Q, $ = e((() => {
	f(), M(), o(), _(), b(), ie(), F(), n(), s(), u(), G = (e, t) => e.error ? (Z(e.error, t), !0) : !1, K = (e, n, r) => {
		a({
			type: t.ERROR,
			message: e,
			details: h(m`${q(n)} ${Y(r)}`),
			delay: 3e4
		});
	}, q = (e) => e.length === 0 ? d : e.length < 80 ? J(e) : m`<vaadin-details class="flex flex-col peer w-full" theme="no-padding reverse">
    <vaadin-details-summary class="font-medium -ms-3 self-start text-secondary text-xs" slot="summary"
      >Details</vaadin-details-summary
    >
    ${J(e)}
  </vaadin-details>`, J = (e) => m`<code class="codeblock"
    >${A(e)}<copilot-copy class="absolute end-0 flex top-0"></copilot-copy
  ></code>`, Y = (e) => e ? m`
    <vaadin-button
      class="peer-has-[[opened]]:mt-2"
      @click="${() => {
		e && v.emit("submit-exception-report-clicked", e);
	}}"
      id="report-issue">
      <vaadin-icon slot="prefix" .svg="${p.bugReport}"></vaadin-icon>
      Report Issue</vaadin-button
    >
  ` : d, X = (e, t, n, r, i) => {
		let a = y.newVaadinVersionState?.versions?.length === 0;
		i && a ? H(i, (n) => {
			K(e, t, n);
		}, e, t, n) : K(e, t), re() && (r?.templateData && typeof r.templateData == "string" && r.templateData.startsWith("data") && (r.templateData = "<IMAGE_DATA>"), v.emit("system-info-with-callback", {
			callback: (t) => v.send("copilot-error", {
				message: e,
				details: String(n).replace("	", "\n") + (r ? `\n \nRequest: \n${JSON.stringify(r)}\n` : ""),
				versions: t
			}),
			notify: !1
		})), y.clearOperationWaitsHmrUpdate();
	}, Z = (e, t) => {
		X(e.message, e.exceptionMessage ?? "", e.exceptionStacktrace?.join("\n") ?? "", t, e.exceptionReport);
	}, Q = [];
}));
//#endregion
export { G as a, F as c, M as d, A as f, z as i, P as l, Q as n, $ as o, L as r, V as s, U as t, N as u };
