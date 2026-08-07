import { n as e } from "./chunk-DiqZc92J.js";
import { at as t, d as n, f as r, i, it as a, nt as o, p as s, r as c, ut as l } from "./icons-Daxdm7Mp.js";
//#region node_modules/@lit/reactive-element/decorators/custom-element.js
var u, d = e((() => {
	u = (e) => (t, n) => {
		n === void 0 ? customElements.define(e, t) : n.addInitializer((() => {
			customElements.define(e, t);
		}));
	};
}));
//#endregion
//#region node_modules/@lit/reactive-element/decorators/property.js
function f(e) {
	return (t, n) => typeof n == "object" ? m(e, t, n) : ((e, t, n) => {
		let r = t.hasOwnProperty(n);
		return t.constructor.createProperty(n, e), r ? Object.getOwnPropertyDescriptor(t, n) : void 0;
	})(e, t, n);
}
var p, m, h = e((() => {
	r(), p = {
		attribute: !0,
		type: String,
		converter: s,
		reflect: !1,
		hasChanged: n
	}, m = (e = p, t, n) => {
		let { kind: r, metadata: i } = n, a = globalThis.litPropertyMetadata.get(i);
		if (a === void 0 && globalThis.litPropertyMetadata.set(i, a = /* @__PURE__ */ new Map()), r === "setter" && ((e = Object.create(e)).wrapped = !0), a.set(n.name, e), r === "accessor") {
			let { name: r } = n;
			return {
				set(n) {
					let i = t.get.call(this);
					t.set.call(this, n), this.requestUpdate(r, i, e);
				},
				init(t) {
					return t !== void 0 && this.C(r, void 0, e, t), t;
				}
			};
		}
		if (r === "setter") {
			let { name: r } = n;
			return function(n) {
				let i = this[r];
				t.call(this, n), this.requestUpdate(r, i, e);
			};
		}
		throw Error("Unsupported decorator location: " + r);
	};
}));
//#endregion
//#region node_modules/@lit/reactive-element/decorators/state.js
function g(e) {
	return f({
		...e,
		state: !0,
		attribute: !1
	});
}
var _ = e((() => {
	h();
})), v = e((() => {})), y, b = e((() => {
	y = (e, t, n) => (n.configurable = !0, n.enumerable = !0, Reflect.decorate && typeof t != "object" && Object.defineProperty(e, t, n), n);
}));
//#endregion
//#region node_modules/@lit/reactive-element/decorators/query.js
function x(e, t) {
	return (n, r, i) => {
		let a = (t) => t.renderRoot?.querySelector(e) ?? null;
		if (t) {
			let { get: e, set: t } = typeof r == "object" ? n : i ?? (() => {
				let e = Symbol();
				return {
					get() {
						return this[e];
					},
					set(t) {
						this[e] = t;
					}
				};
			})();
			return y(n, r, { get() {
				let n = e.call(this);
				return n === void 0 && (n = a(this), (n !== null || this.hasUpdated) && t.call(this, n)), n;
			} });
		}
		return y(n, r, { get() {
			return a(this);
		} });
	};
}
var S = e((() => {
	b();
})), C = e((() => {})), w = e((() => {})), T = e((() => {})), E = e((() => {})), D = e((() => {
	d(), h(), _(), v(), S(), C(), w(), T(), E();
}));
//#endregion
//#region \0@oxc-project+runtime@0.123.0/helpers/decorate.js
function O(e, t, n, r) {
	var i = arguments.length, a = i < 3 ? t : r === null ? r = Object.getOwnPropertyDescriptor(t, n) : r, o;
	if (typeof Reflect == "object" && typeof Reflect.decorate == "function") a = Reflect.decorate(e, t, n, r);
	else for (var s = e.length - 1; s >= 0; s--) (o = e[s]) && (a = (i < 3 ? o(a) : i > 3 ? o(t, n, a) : o(t, n)) || a);
	return i > 3 && a && Object.defineProperty(t, n, a), a;
}
var k = e((() => {}));
//#endregion
//#region node_modules/@adobe/lit-mobx/lib/mixin-custom.js
function A(e, t) {
	var n, r;
	return r = class extends e {
		constructor() {
			super(...arguments), this[n] = () => {
				this.requestUpdate();
			};
		}
		connectedCallback() {
			super.connectedCallback(), this[j] = new t(`${this.constructor.name || this.nodeName}.update()`, this[M]), this.hasUpdated && this.requestUpdate();
		}
		disconnectedCallback() {
			super.disconnectedCallback(), this[j] && (this[j].dispose(), this[j] = void 0);
		}
		update(e) {
			this[j] ? this[j].track(super.update.bind(this, e)) : super.update(e);
		}
	}, n = M, r;
}
var j, M, N = e((() => {
	j = Symbol("LitMobxRenderReaction"), M = Symbol("LitMobxRequestUpdate");
}));
//#endregion
//#region node_modules/@adobe/lit-mobx/lib/mixin.js
function P(e) {
	return A(e, o);
}
var F = e((() => {
	N(), t();
})), I, L = e((() => {
	F(), c(), F(), I = class extends P(i) {};
})), R, z = e((() => {
	L(), t(), R = class extends I {
		constructor(...e) {
			super(...e), this.disposers = [];
		}
		reaction(e, t, n) {
			this.disposers.push(l(e, t, n));
		}
		autorun(e, t) {
			this.disposers.push(a(e, t));
		}
		disconnectedCallback() {
			super.disconnectedCallback(), this.disposers.forEach((e) => {
				e();
			}), this.disposers = [];
		}
	};
})), B, V = e((() => {
	if (B = window.Vaadin.copilot._sectionPanelUiState, !B) throw Error("Tried to access copilot section panel ui state before it was initialized.");
}));
//#endregion
export { O as a, x as c, u as d, z as i, g as l, B as n, k as o, R as r, D as s, V as t, f as u };
