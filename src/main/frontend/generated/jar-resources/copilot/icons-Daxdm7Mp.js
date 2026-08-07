import { n as e } from "./chunk-DiqZc92J.js";
//#region node_modules/mobx/dist/mobx.esm.js
function t(e) {
	var t = [...arguments].slice(1);
	if (process.env.NODE_ENV !== "production") {
		var n = typeof e == "string" ? e : Bn[e];
		throw typeof n == "function" && (n = n.apply(null, t)), Error("[MobX] " + n);
	}
	throw Error(typeof e == "number" ? "[MobX] minified error nr: " + e + (t.length ? " " + t.map(String).join(",") : "") + ". Find the full error at: https://github.com/mobxjs/mobx/blob/main/packages/mobx/src/errors.ts" : "[MobX] " + e);
}
function n() {
	return typeof globalThis < "u" ? globalThis : typeof window < "u" ? window : typeof global < "u" ? global : typeof self < "u" ? self : Vn;
}
function r() {
	qn || t(process.env.NODE_ENV === "production" ? "Proxy not available" : "`Proxy` objects are not available in the current environment. Please configure MobX to enable a fallback implementation.`");
}
function i(e) {
	process.env.NODE_ENV !== "production" && W.verifyProxies && t("MobX is currently configured to be able to run in ES5 mode, but in ES5 MobX won't be able to " + e);
}
function a() {
	return ++W.mobxGuid;
}
function o(e) {
	var t = !1;
	return function() {
		if (!t) return t = !0, e.apply(this, arguments);
	};
}
function s(e) {
	return typeof e == "function";
}
function c(e) {
	switch (typeof e) {
		case "string":
		case "symbol":
		case "number": return !0;
	}
	return !1;
}
function l(e) {
	return typeof e == "object" && !!e;
}
function u(e) {
	if (!l(e)) return !1;
	var t = Object.getPrototypeOf(e);
	if (t == null) return !0;
	var n = Object.hasOwnProperty.call(t, "constructor") && t.constructor;
	return typeof n == "function" && n.toString() === Jn;
}
function d(e) {
	var t = e?.constructor;
	return t ? t.name === "GeneratorFunction" || t.displayName === "GeneratorFunction" : !1;
}
function f(e, t, n) {
	I(e, t, {
		enumerable: !1,
		writable: !0,
		configurable: !0,
		value: n
	});
}
function ee(e, t, n) {
	I(e, t, {
		enumerable: !1,
		writable: !1,
		configurable: !0,
		value: n
	});
}
function p(e, t) {
	var n = "isMobX" + e;
	return t.prototype[n] = !0, function(e) {
		return l(e) && e[n] === !0;
	};
}
function m(e) {
	return e != null && Object.prototype.toString.call(e) === "[object Map]";
}
function h(e) {
	return Object.getPrototypeOf(Object.getPrototypeOf(Object.getPrototypeOf(e))) === null;
}
function g(e) {
	return e != null && Object.prototype.toString.call(e) === "[object Set]";
}
function te(e) {
	var t = Object.keys(e);
	if (!Xn) return t;
	var n = Object.getOwnPropertySymbols(e);
	return n.length ? [].concat(t, n.filter(function(t) {
		return Wn.propertyIsEnumerable.call(e, t);
	})) : t;
}
function ne(e) {
	return typeof e == "string" ? e : typeof e == "symbol" ? e.toString() : new String(e).toString();
}
function re(e) {
	return e === null ? null : typeof e == "object" ? "" + e : e;
}
function _(e, t) {
	return Wn.hasOwnProperty.call(e, t);
}
function v(e, t) {
	return !!(e & t);
}
function y(e, t, n) {
	return n ? e |= t : e &= ~t, e;
}
function ie(e, t) {
	(t == null || t > e.length) && (t = e.length);
	for (var n = 0, r = Array(t); n < t; n++) r[n] = e[n];
	return r;
}
function ae(e, t) {
	for (var n = 0; n < t.length; n++) {
		var r = t[n];
		r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), Object.defineProperty(e, fe(r.key), r);
	}
}
function oe(e, t, n) {
	return t && ae(e.prototype, t), n && ae(e, n), Object.defineProperty(e, "prototype", { writable: !1 }), e;
}
function se(e, t) {
	var n = typeof Symbol < "u" && e[Symbol.iterator] || e["@@iterator"];
	if (n) return (n = n.call(e)).next.bind(n);
	if (Array.isArray(e) || (n = pe(e)) || t && e && typeof e.length == "number") {
		n && (e = n);
		var r = 0;
		return function() {
			return r >= e.length ? { done: !0 } : {
				done: !1,
				value: e[r++]
			};
		};
	}
	throw TypeError("Invalid attempt to iterate non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method.");
}
function ce() {
	return ce = Object.assign ? Object.assign.bind() : function(e) {
		for (var t = 1; t < arguments.length; t++) {
			var n = arguments[t];
			for (var r in n) ({}).hasOwnProperty.call(n, r) && (e[r] = n[r]);
		}
		return e;
	}, ce.apply(null, arguments);
}
function le(e, t) {
	e.prototype = Object.create(t.prototype), e.prototype.constructor = e, ue(e, t);
}
function ue(e, t) {
	return ue = Object.setPrototypeOf ? Object.setPrototypeOf.bind() : function(e, t) {
		return e.__proto__ = t, e;
	}, ue(e, t);
}
function de(e, t) {
	if (typeof e != "object" || !e) return e;
	var n = e[Symbol.toPrimitive];
	if (n !== void 0) {
		var r = n.call(e, t || "default");
		if (typeof r != "object") return r;
		throw TypeError("@@toPrimitive must return a primitive value.");
	}
	return (t === "string" ? String : Number)(e);
}
function fe(e) {
	var t = de(e, "string");
	return typeof t == "symbol" ? t : t + "";
}
function pe(e, t) {
	if (e) {
		if (typeof e == "string") return ie(e, t);
		var n = {}.toString.call(e).slice(8, -1);
		return n === "Object" && e.constructor && (n = e.constructor.name), n === "Map" || n === "Set" ? Array.from(e) : n === "Arguments" || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n) ? ie(e, t) : void 0;
	}
}
function b(e) {
	function t(t, n) {
		if (ge(n)) return e.decorate_20223_(t, n);
		me(t, n, e);
	}
	return Object.assign(t, e);
}
function me(e, n, r) {
	_(e, L) || f(e, L, ce({}, e[L])), process.env.NODE_ENV !== "production" && De(r) && !_(e[L], n) && t("'" + (e.constructor.name + ".prototype." + n.toString()) + "' is decorated with 'override', but no such decorated member was found on prototype."), he(e, r, n), De(r) || (e[L][n] = r);
}
function he(e, n, r) {
	if (process.env.NODE_ENV !== "production" && !De(n) && _(e[L], r)) {
		var i = e.constructor.name + ".prototype." + r.toString(), a = e[L][r].annotationType_, o = n.annotationType_;
		t("Cannot apply '@" + o + "' to '" + i + "':" + ("\nThe field is already decorated with '@" + a + "'.") + "\nRe-decorating fields is not allowed.\nUse '@override' decorator for methods overridden by subclass.");
	}
}
function ge(e) {
	return typeof e == "object" && typeof e.kind == "string";
}
function _e(e, n) {
	process.env.NODE_ENV !== "production" && !n.includes(e.kind) && t("The decorator applied to '" + String(e.name) + "' cannot be used on a " + e.kind + " element");
}
function ve(e, t, n) {
	t === void 0 && (t = Yn), n === void 0 && (n = Yn);
	var r = new z(e);
	return t !== Yn && Gt(r, t), n !== Yn && Kt(r, n), r;
}
function ye(e, t) {
	return e === t;
}
function be(e, t) {
	return Pn(e, t);
}
function xe(e, t) {
	return Pn(e, t, 1);
}
function Se(e, t) {
	return Object.is ? Object.is(e, t) : e === t ? e !== 0 || 1 / e == 1 / t : e !== e && t !== t;
}
function Ce(e, t, n) {
	return nn(e) ? e : Array.isArray(e) ? B.array(e, { name: n }) : u(e) ? B.object(e, void 0, { name: n }) : m(e) ? B.map(e, { name: n }) : g(e) ? B.set(e, { name: n }) : typeof e == "function" && !Bt(e) && !en(e) ? d(e) ? Xr(e) : Ur(n, e) : e;
}
function we(e, n, r) {
	if (e == null || Sn(e) || gn(e) || Y(e) || X(e)) return e;
	if (Array.isArray(e)) return B.array(e, {
		name: r,
		deep: !1
	});
	if (u(e)) return B.object(e, void 0, {
		name: r,
		deep: !1
	});
	if (m(e)) return B.map(e, {
		name: r,
		deep: !1
	});
	if (g(e)) return B.set(e, {
		name: r,
		deep: !1
	});
	process.env.NODE_ENV !== "production" && t("The shallow modifier / decorator can only used in combination with arrays, objects, maps and sets");
}
function Te(e) {
	return e;
}
function Ee(e, n) {
	return process.env.NODE_ENV !== "production" && nn(e) && t("observable.struct should not be used with observable values"), Pn(e, n) ? n : e;
}
function De(e) {
	return e.annotationType_ === tr;
}
function Oe(e, t) {
	return {
		annotationType_: e,
		options_: t,
		make_: ke,
		extend_: Ae,
		decorate_20223_: je
	};
}
function ke(e, t, n, r) {
	var i;
	return (i = this.options_) != null && i.bound ? this.extend_(e, t, n, !1) === null ? 0 : 1 : r === e.target_ ? this.extend_(e, t, n, !1) === null ? 0 : 2 : Bt(n.value) ? 1 : (I(r, t, Ne(e, this, t, n, !1)), 2);
}
function Ae(e, t, n, r) {
	var i = Ne(e, this, t, n);
	return e.defineProperty_(t, i, r);
}
function je(e, n) {
	process.env.NODE_ENV !== "production" && _e(n, ["method", "field"]);
	var r = n.kind, i = n.name, a = n.addInitializer, o = this, s = function(e) {
		return at(o.options_?.name ?? i.toString(), e, o.options_?.autoAction ?? !1);
	};
	if (r == "field") return function(e) {
		var t, n = e;
		return Bt(n) || (n = s(n)), (t = o.options_) != null && t.bound && (n = n.bind(this), n.isMobxAction = !0), n;
	};
	if (r == "method") {
		var c;
		return Bt(e) || (e = s(e)), (c = this.options_) != null && c.bound && a(function() {
			var e = this, t = e[i].bind(e);
			t.isMobxAction = !0, e[i] = t;
		}), e;
	}
	t("Cannot apply '" + o.annotationType_ + "' to '" + String(i) + "' (kind: " + r + "):" + ("\n'" + o.annotationType_ + "' can only be used on properties with a function value."));
}
function Me(e, n, r, i) {
	var a = n.annotationType_, o = i.value;
	process.env.NODE_ENV !== "production" && !s(o) && t("Cannot apply '" + a + "' to '" + e.name_ + "." + r.toString() + "':" + ("\n'" + a + "' can only be used on properties with a function value."));
}
function Ne(e, t, n, r, i) {
	var a, o;
	i === void 0 && (i = W.safeDescriptors), Me(e, t, n, r);
	var s = r.value;
	return (a = t.options_) != null && a.bound && (s = s.bind(e.proxy_ ?? e.target_)), {
		value: at(t.options_?.name ?? n.toString(), s, t.options_?.autoAction ?? !1, (o = t.options_) != null && o.bound ? e.proxy_ ?? e.target_ : void 0),
		configurable: i ? e.isPlainObject_ : !0,
		enumerable: !1,
		writable: !i
	};
}
function Pe(e, t) {
	return {
		annotationType_: e,
		options_: t,
		make_: Fe,
		extend_: Ie,
		decorate_20223_: Le
	};
}
function Fe(e, t, n, r) {
	var i;
	return r === e.target_ ? this.extend_(e, t, n, !1) === null ? 0 : 2 : (i = this.options_) != null && i.bound && (!_(e.target_, t) || !en(e.target_[t])) && this.extend_(e, t, n, !1) === null ? 0 : en(n.value) ? 1 : (I(r, t, ze(e, this, t, n, !1, !1)), 2);
}
function Ie(e, t, n, r) {
	var i = ze(e, this, t, n, this.options_?.bound);
	return e.defineProperty_(t, i, r);
}
function Le(e, t) {
	var n;
	process.env.NODE_ENV !== "production" && _e(t, ["method"]);
	var r = t.name, i = t.addInitializer;
	return en(e) || (e = Xr(e)), (n = this.options_) != null && n.bound && i(function() {
		var e = this, t = e[r].bind(e);
		t.isMobXFlow = !0, e[r] = t;
	}), e;
}
function Re(e, n, r, i) {
	var a = n.annotationType_, o = i.value;
	process.env.NODE_ENV !== "production" && !s(o) && t("Cannot apply '" + a + "' to '" + e.name_ + "." + r.toString() + "':" + ("\n'" + a + "' can only be used on properties with a generator function value."));
}
function ze(e, t, n, r, i, a) {
	a === void 0 && (a = W.safeDescriptors), Re(e, t, n, r);
	var o = r.value;
	return en(o) || (o = Xr(o)), i && (o = o.bind(e.proxy_ ?? e.target_), o.isMobXFlow = !0), {
		value: o,
		configurable: a ? e.isPlainObject_ : !0,
		enumerable: !1,
		writable: !a
	};
}
function Be(e, t) {
	return {
		annotationType_: e,
		options_: t,
		make_: Ve,
		extend_: He,
		decorate_20223_: Ue
	};
}
function Ve(e, t, n) {
	return this.extend_(e, t, n, !1) === null ? 0 : 1;
}
function He(e, t, n, r) {
	return We(e, this, t, n), e.defineComputedProperty_(t, ce({}, this.options_, {
		get: n.get,
		set: n.set
	}), r);
}
function Ue(e, t) {
	process.env.NODE_ENV !== "production" && _e(t, ["getter"]);
	var n = this, r = t.name, i = t.addInitializer;
	return i(function() {
		var t = bn(this)[R], i = ce({}, n.options_, {
			get: e,
			context: this
		});
		i.name ||= process.env.NODE_ENV === "production" ? "ObservableObject." + r.toString() : t.name_ + "." + r.toString(), t.values_.set(r, new V(i));
	}), function() {
		return this[R].getObservablePropValue_(r);
	};
}
function We(e, n, r, i) {
	var a = n.annotationType_, o = i.get;
	process.env.NODE_ENV !== "production" && !o && t("Cannot apply '" + a + "' to '" + e.name_ + "." + r.toString() + "':" + ("\n'" + a + "' can only be used on getter(+setter) properties."));
}
function Ge(e, t) {
	return {
		annotationType_: e,
		options_: t,
		make_: Ke,
		extend_: qe,
		decorate_20223_: Je
	};
}
function Ke(e, t, n) {
	return this.extend_(e, t, n, !1) === null ? 0 : 1;
}
function qe(e, t, n, r) {
	return Ye(e, this, t, n), e.defineObservableProperty_(t, n.value, this.options_?.enhancer ?? Ce, r);
}
function Je(e, n) {
	if (process.env.NODE_ENV !== "production") {
		if (n.kind === "field") throw t("Please use `@observable accessor " + String(n.name) + "` instead of `@observable " + String(n.name) + "`");
		_e(n, ["accessor"]);
	}
	var r = this, i = n.kind, a = n.name, o = /* @__PURE__ */ new WeakSet();
	function s(e, t) {
		var n = bn(e)[R], i = new wr(t, r.options_?.enhancer ?? Ce, process.env.NODE_ENV === "production" ? "ObservableObject." + a.toString() : n.name_ + "." + a.toString(), !1);
		n.values_.set(a, i), o.add(e);
	}
	if (i == "accessor") return {
		get: function() {
			return o.has(this) || s(this, e.get.call(this)), this[R].getObservablePropValue_(a);
		},
		set: function(e) {
			return o.has(this) || s(this, e), this[R].setObservablePropValue_(a, e);
		},
		init: function(e) {
			return o.has(this) || s(this, e), e;
		}
	};
}
function Ye(e, n, r, i) {
	var a = n.annotationType_;
	process.env.NODE_ENV !== "production" && !("value" in i) && t("Cannot apply '" + a + "' to '" + e.name_ + "." + r.toString() + "':" + ("\n'" + a + "' cannot be used on getter/setter properties"));
}
function Xe(e) {
	return {
		annotationType_: nr,
		options_: e,
		make_: Ze,
		extend_: Qe,
		decorate_20223_: $e
	};
}
function Ze(e, t, n, r) {
	var i;
	if (n.get) return vr.make_(e, t, n, r);
	if (n.set) {
		var a = Bt(n.set) ? n.set : at(t.toString(), n.set);
		return r === e.target_ ? e.defineProperty_(t, {
			configurable: W.safeDescriptors ? e.isPlainObject_ : !0,
			set: a
		}) === null ? 0 : 2 : (I(r, t, {
			configurable: !0,
			set: a
		}), 2);
	}
	if (r !== e.target_ && typeof n.value == "function") {
		var o;
		if (d(n.value)) {
			var s;
			return ((s = this.options_) != null && s.autoBind ? Xr.bound : Xr).make_(e, t, n, r);
		}
		return ((o = this.options_) != null && o.autoBind ? Ur.bound : Ur).make_(e, t, n, r);
	}
	var c = this.options_?.deep === !1 ? B.ref : B;
	return typeof n.value == "function" && (i = this.options_) != null && i.autoBind && (n.value = n.value.bind(e.proxy_ ?? e.target_)), c.make_(e, t, n, r);
}
function Qe(e, t, n, r) {
	var i;
	return n.get ? vr.extend_(e, t, n, r) : n.set ? e.defineProperty_(t, {
		configurable: W.safeDescriptors ? e.isPlainObject_ : !0,
		set: at(t.toString(), n.set)
	}, r) : (typeof n.value == "function" && (i = this.options_) != null && i.autoBind && (n.value = n.value.bind(e.proxy_ ?? e.target_)), (this.options_?.deep === !1 ? B.ref : B).extend_(e, t, n, r));
}
function $e(e, n) {
	t("'" + this.annotationType_ + "' cannot be used as a decorator");
}
function et(e) {
	return e || cr;
}
function tt(e) {
	return e.deep === !0 ? Ce : e.deep === !1 ? Te : rt(e.defaultDecorator);
}
function nt(e) {
	return e ? e.defaultDecorator ?? Xe(e) : void 0;
}
function rt(e) {
	return e ? e.options_?.enhancer ?? Ce : Ce;
}
function it(e, t, n) {
	if (ge(t)) return lr.decorate_20223_(e, t);
	if (c(t)) {
		me(e, t, lr);
		return;
	}
	return nn(e) ? e : u(e) ? B.object(e, t, n) : Array.isArray(e) ? B.array(e, t) : m(e) ? B.map(e, t) : g(e) ? B.set(e, t) : typeof e == "object" && e ? e : B.box(e, t);
}
function at(e, n, r, i) {
	r === void 0 && (r = !1), process.env.NODE_ENV !== "production" && (s(n) || t("`action` can only be invoked on functions"), (typeof e != "string" || !e) && t("actions should have valid names, got: '" + e + "'"));
	function a() {
		return ot(e, r, n, i || this, arguments);
	}
	return a.isMobxAction = !0, a.toString = function() {
		return n.toString();
	}, xr && (Sr.value = e, I(a, "name", Sr)), a;
}
function ot(e, t, n, r, i) {
	var a = st(e, t, r, i);
	try {
		return n.apply(r, i);
	} catch (e) {
		throw a.error_ = e, e;
	} finally {
		ct(a);
	}
}
function st(e, t, n, r) {
	var i = process.env.NODE_ENV !== "production" && T() && !!e, a = 0;
	process.env.NODE_ENV !== "production" && i && (a = Date.now(), E({
		type: Pr,
		name: e,
		object: n,
		arguments: r ? Array.from(r) : Gn
	}));
	var o = W.trackingDerivation, s = !t || !o;
	C();
	var c = W.allowStateChanges;
	s && (bt(), c = ut(!0));
	var l = xt(!0), u = {
		runAsAction_: s,
		prevDerivation_: o,
		prevAllowStateChanges_: c,
		prevAllowStateReads_: l,
		notifySpy_: i,
		startTime_: a,
		actionId_: br++,
		parentActionId_: yr
	};
	return yr = u.actionId_, u;
}
function ct(e) {
	yr !== e.actionId_ && t(30), yr = e.parentActionId_, e.error_ !== void 0 && (W.suppressReactionErrors = !0), dt(e.prevAllowStateChanges_), St(e.prevAllowStateReads_), w(), e.runAsAction_ && S(e.prevDerivation_), process.env.NODE_ENV !== "production" && e.notifySpy_ && D({ time: Date.now() - e.startTime_ }), W.suppressReactionErrors = !1;
}
function lt(e, t) {
	var n = ut(e);
	try {
		return t();
	} finally {
		dt(n);
	}
}
function ut(e) {
	var t = W.allowStateChanges;
	return W.allowStateChanges = e, t;
}
function dt(e) {
	W.allowStateChanges = e;
}
function ft(e) {
	return e instanceof Er;
}
function pt(e) {
	switch (e.dependenciesState_) {
		case H.UP_TO_DATE_: return !1;
		case H.NOT_TRACKING_:
		case H.STALE_: return !0;
		case H.POSSIBLY_STALE_:
			for (var t = xt(!0), n = bt(), r = e.observing_, i = r.length, a = 0; a < i; a++) {
				var o = r[a];
				if (Tr(o)) {
					if (W.disableErrorBoundaries) o.get();
					else try {
						o.get();
					} catch {
						return S(n), St(t), !0;
					}
					if (e.dependenciesState_ === H.STALE_) return S(n), St(t), !0;
				}
			}
			return Ct(e), S(n), St(t), !1;
	}
}
function x(e) {
	if (process.env.NODE_ENV !== "production") {
		var t = e.observers_.size > 0;
		!W.allowStateChanges && (t || W.enforceActions === "always") && console.warn("[MobX] " + (W.enforceActions ? "Since strict-mode is enabled, changing (observed) observable values without using an action is not allowed. Tried to modify: " : "Side effects like changing state are not allowed at this point. Are you trying to modify state from, for example, a computed value or the render function of a React component? You can wrap side effects in 'runInAction' (or decorate functions with 'action') if needed. Tried to modify: ") + e.name_);
	}
}
function mt(e) {
	process.env.NODE_ENV !== "production" && !W.allowStateReads && W.observableRequiresReaction && console.warn("[mobx] Observable '" + e.name_ + "' being read outside a reactive context.");
}
function ht(e, t, n) {
	var r = xt(!0);
	Ct(e), e.newObserving_ = Array(e.runId_ === 0 ? 100 : e.observing_.length), e.unboundDepsCount_ = 0, e.runId_ = ++W.runId;
	var i = W.trackingDerivation;
	W.trackingDerivation = e, W.inBatch++;
	var a;
	if (W.disableErrorBoundaries === !0) a = t.call(n);
	else try {
		a = t.call(n);
	} catch (e) {
		a = new Er(e);
	}
	return W.inBatch--, W.trackingDerivation = i, _t(e), gt(e), St(r), a;
}
function gt(e) {
	process.env.NODE_ENV !== "production" && e.observing_.length === 0 && (typeof e.requiresObservable_ == "boolean" ? e.requiresObservable_ : W.reactionRequiresObservable) && console.warn("[mobx] Derivation '" + e.name_ + "' is created/updated without reading any observable value.");
}
function _t(e) {
	for (var t = e.observing_, n = e.observing_ = e.newObserving_, r = H.UP_TO_DATE_, i = 0, a = e.unboundDepsCount_, o = 0; o < a; o++) {
		var s = n[o];
		s.diffValue === 0 && (s.diffValue = 1, i !== o && (n[i] = s), i++), s.dependenciesState_ > r && (r = s.dependenciesState_);
	}
	for (n.length = i, e.newObserving_ = null, a = t.length; a--;) {
		var c = t[a];
		c.diffValue === 0 && Tt(c, e), c.diffValue = 0;
	}
	for (; i--;) {
		var l = n[i];
		l.diffValue === 1 && (l.diffValue = 0, wt(l, e));
	}
	r !== H.UP_TO_DATE_ && (e.dependenciesState_ = r, e.onBecomeStale_());
}
function vt(e) {
	var t = e.observing_;
	e.observing_ = [];
	for (var n = t.length; n--;) Tt(t[n], e);
	e.dependenciesState_ = H.NOT_TRACKING_;
}
function yt(e) {
	var t = bt();
	try {
		return e();
	} finally {
		S(t);
	}
}
function bt() {
	var e = W.trackingDerivation;
	return W.trackingDerivation = null, e;
}
function S(e) {
	W.trackingDerivation = e;
}
function xt(e) {
	var t = W.allowStateReads;
	return W.allowStateReads = e, t;
}
function St(e) {
	W.allowStateReads = e;
}
function Ct(e) {
	if (e.dependenciesState_ !== H.UP_TO_DATE_) {
		e.dependenciesState_ = H.UP_TO_DATE_;
		for (var t = e.observing_, n = t.length; n--;) t[n].lowestObserverState_ = H.UP_TO_DATE_;
	}
}
function wt(e, t) {
	e.observers_.add(t), e.lowestObserverState_ > t.dependenciesState_ && (e.lowestObserverState_ = t.dependenciesState_);
}
function Tt(e, t) {
	e.observers_.delete(t), e.observers_.size === 0 && Et(e);
}
function Et(e) {
	e.isPendingUnobservation === !1 && (e.isPendingUnobservation = !0, W.pendingUnobservations.push(e));
}
function C() {
	W.inBatch++;
}
function w() {
	if (--W.inBatch === 0) {
		Pt();
		for (var e = W.pendingUnobservations, t = 0; t < e.length; t++) {
			var n = e[t];
			n.isPendingUnobservation = !1, n.observers_.size === 0 && (n.isBeingObserved && (n.isBeingObserved = !1, n.onBUO()), n instanceof V && n.suspend_());
		}
		W.pendingUnobservations = [];
	}
}
function Dt(e) {
	mt(e);
	var t = W.trackingDerivation;
	return t === null ? (e.observers_.size === 0 && W.inBatch > 0 && Et(e), !1) : (t.runId_ !== e.lastAccessedBy_ && (e.lastAccessedBy_ = t.runId_, t.newObserving_[t.unboundDepsCount_++] = e, !e.isBeingObserved && W.trackingContext && (e.isBeingObserved = !0, e.onBO())), e.isBeingObserved);
}
function Ot(e) {
	e.lowestObserverState_ !== H.STALE_ && (e.lowestObserverState_ = H.STALE_, e.observers_.forEach(function(t) {
		t.dependenciesState_ === H.UP_TO_DATE_ && (process.env.NODE_ENV !== "production" && t.isTracing_ !== U.NONE && jt(t, e), t.onBecomeStale_()), t.dependenciesState_ = H.STALE_;
	}));
}
function kt(e) {
	e.lowestObserverState_ !== H.STALE_ && (e.lowestObserverState_ = H.STALE_, e.observers_.forEach(function(t) {
		t.dependenciesState_ === H.POSSIBLY_STALE_ ? (t.dependenciesState_ = H.STALE_, process.env.NODE_ENV !== "production" && t.isTracing_ !== U.NONE && jt(t, e)) : t.dependenciesState_ === H.UP_TO_DATE_ && (e.lowestObserverState_ = H.UP_TO_DATE_);
	}));
}
function At(e) {
	e.lowestObserverState_ === H.UP_TO_DATE_ && (e.lowestObserverState_ = H.POSSIBLY_STALE_, e.observers_.forEach(function(e) {
		e.dependenciesState_ === H.UP_TO_DATE_ && (e.dependenciesState_ = H.POSSIBLY_STALE_, e.onBecomeStale_());
	}));
}
function jt(e, t) {
	if (console.log("[mobx.trace] '" + e.name_ + "' is invalidated due to a change in: '" + t.name_ + "'"), e.isTracing_ === U.BREAK) {
		var n = [];
		Mt(Yt(e), n, 1), Function("debugger;\n/*\nTracing '" + e.name_ + "'\n\nYou are entering this break point because derivation '" + e.name_ + "' is being traced and '" + t.name_ + "' is now forcing it to update.\nJust follow the stacktrace you should now see in the devtools to see precisely what piece of your code is causing this update\nThe stackframe you are looking for is at least ~6-8 stack-frames up.\n\n" + (e instanceof V ? e.derivation.toString().replace(/[*]\//g, "/") : "") + "\n\nThe dependencies for this derivation are:\n\n" + n.join("\n") + "\n*/\n    ")();
	}
}
function Mt(e, t, n) {
	if (t.length >= 1e3) {
		t.push("(and many more)");
		return;
	}
	t.push("" + "	".repeat(n - 1) + e.name), e.dependencies && e.dependencies.forEach(function(e) {
		return Mt(e, t, n + 1);
	});
}
function Nt(e) {
	return W.globalReactionErrorHandlers.push(e), function() {
		var t = W.globalReactionErrorHandlers.indexOf(e);
		t >= 0 && W.globalReactionErrorHandlers.splice(t, 1);
	};
}
function Pt() {
	W.inBatch > 0 || W.isRunningReactions || jr(Ft);
}
function Ft() {
	W.isRunningReactions = !0;
	for (var e = W.pendingReactions, t = 0; e.length > 0;) {
		++t === Ar && (console.error(process.env.NODE_ENV === "production" ? "[mobx] cycle in reaction: " + e[0] : "Reaction doesn't converge to a stable state after " + Ar + " iterations." + (" Probably there is a cycle in the reactive function: " + e[0])), e.splice(0));
		for (var n = e.splice(0), r = 0, i = n.length; r < i; r++) n[r].runReaction_();
	}
	W.isRunningReactions = !1;
}
function T() {
	return process.env.NODE_ENV !== "production" && !!W.spyListeners.length;
}
function It(e) {
	if (process.env.NODE_ENV !== "production" && W.spyListeners.length) for (var t = W.spyListeners, n = 0, r = t.length; n < r; n++) t[n](e);
}
function E(e) {
	process.env.NODE_ENV !== "production" && It(ce({}, e, { spyReportStart: !0 }));
}
function D(e) {
	process.env.NODE_ENV !== "production" && It(e ? ce({}, e, {
		type: "report-end",
		spyReportEnd: !0
	}) : Nr);
}
function Lt(e) {
	return process.env.NODE_ENV === "production" ? (console.warn("[mobx.spy] Is a no-op in production builds"), function() {}) : (W.spyListeners.push(e), o(function() {
		W.spyListeners = W.spyListeners.filter(function(t) {
			return t !== e;
		});
	}));
}
function Rt(e) {
	return function(n, r) {
		if (s(n)) return at(n.name || Rr, n, e);
		if (s(r)) return at(n, r, e);
		if (ge(r)) return (e ? Vr : zr).decorate_20223_(n, r);
		if (c(r)) return me(n, r, e ? Vr : zr);
		if (c(n)) return b(Oe(e ? Ir : Pr, {
			name: n,
			autoAction: e
		}));
		process.env.NODE_ENV !== "production" && t("Invalid arguments for `action`");
	};
}
function zt(e) {
	return ot(e.name || Rr, !1, e, this, void 0);
}
function Bt(e) {
	return s(e) && e.isMobxAction === !0;
}
function Vt(e, n) {
	var r;
	n === void 0 && (n = Kn), process.env.NODE_ENV !== "production" && (s(e) || t("Autorun expects a function as first argument"), Bt(e) && t("Autorun does not accept actions since actions are untrackable"));
	var i = n?.name ?? (process.env.NODE_ENV === "production" ? "Autorun" : e.name || "Autorun@" + a()), o = !n.scheduler && !n.delay, c;
	if (o) c = new G(i, function() {
		this.track(d);
	}, n.onError, n.requiresObservable);
	else {
		var l = Ht(n), u = !1;
		c = new G(i, function() {
			u || (u = !0, l(function() {
				u = !1, c.isDisposed || c.track(d);
			}));
		}, n.onError, n.requiresObservable);
	}
	function d() {
		e(c);
	}
	return (r = n) != null && (r = r.signal) != null && r.aborted || c.schedule_(), c.getDisposer_(n?.signal);
}
function Ht(e) {
	return e.scheduler ? e.scheduler : e.delay ? function(t) {
		return setTimeout(t, e.delay);
	} : Wr;
}
function Ut(e, n, r) {
	var i;
	r === void 0 && (r = Kn), process.env.NODE_ENV !== "production" && ((!s(e) || !s(n)) && t("First and second argument to reaction should be functions"), u(r) || t("Third argument of reactions should be an object"));
	var o = r.name ?? (process.env.NODE_ENV === "production" ? "Reaction" : "Reaction@" + a()), c = K(o, r.onError ? Wt(r.onError, n) : n), l = !r.scheduler && !r.delay, d = Ht(r), f = !0, ee = !1, p, m = r.compareStructural ? er.structural : r.equals || er.default, h = new G(o, function() {
		f || l ? g() : ee || (ee = !0, d(g));
	}, r.onError, r.requiresObservable);
	function g() {
		if (ee = !1, !h.isDisposed) {
			var t = !1, n = p;
			h.track(function() {
				var n = lt(!1, function() {
					return e(h);
				});
				t = f || !m(p, n), p = n;
			}), (f && r.fireImmediately || !f && t) && c(p, n, h), f = !1;
		}
	}
	return (i = r) != null && (i = i.signal) != null && i.aborted || h.schedule_(), h.getDisposer_(r?.signal);
}
function Wt(e, t) {
	return function() {
		try {
			return t.apply(this, arguments);
		} catch (t) {
			e.call(this, t);
		}
	};
}
function Gt(e, t, n) {
	return qt(Gr, e, t, n);
}
function Kt(e, t, n) {
	return qt(Kr, e, t, n);
}
function qt(e, t, n, r) {
	var i = typeof r == "function" ? An(t, n) : An(t), a = s(r) ? r : n, o = e + "L";
	return i[o] ? i[o].add(a) : i[o] = new Set([a]), function() {
		var e = i[o];
		e && (e.delete(a), e.size === 0 && delete i[o]);
	};
}
function Jt(e, n, r, i) {
	process.env.NODE_ENV !== "production" && (arguments.length > 4 && t("'extendObservable' expected 2-4 arguments"), typeof e != "object" && t("'extendObservable' expects an object as first argument"), Y(e) && t("'extendObservable' should not be used on maps, use map.merge instead"), u(n) || t("'extendObservable' only accepts plain objects as second argument"), (nn(n) || nn(r)) && t("Extending an object with another observable (object) is not supported"));
	var a = Qn(n);
	return Nn(function() {
		var t = bn(e, i)[R];
		Zn(a).forEach(function(e) {
			t.extend_(e, a[e], r && e in r ? r[e] : !0);
		});
	}), e;
}
function Yt(e, t) {
	return Xt(An(e, t));
}
function Xt(e) {
	var t = { name: e.name_ };
	return e.observing_ && e.observing_.length > 0 && (t.dependencies = Zt(e.observing_).map(Xt)), t;
}
function Zt(e) {
	return Array.from(new Set(e));
}
function Qt() {
	this.message = "FLOW_CANCELLED";
}
function $t(e) {
	s(e.cancel) && e.cancel();
}
function en(e) {
	return e?.isMobXFlow === !0;
}
function tn(e, n) {
	return e ? n === void 0 ? Sn(e) || !!e[R] || $n(e) || Mr(e) || Tr(e) : process.env.NODE_ENV !== "production" && (Y(e) || gn(e)) ? t("isObservable(object, propertyName) is not supported for arrays and maps. Use map.has or array.length instead.") : Sn(e) ? e[R].values_.has(n) : !1 : !1;
}
function nn(e) {
	return process.env.NODE_ENV !== "production" && arguments.length !== 1 && t("isObservable expects only 1 argument. Use isObservableProp to inspect the observability of a property"), tn(e);
}
function rn(e, t, n, r) {
	return s(n) ? on(e, t, n, r) : an(e, t, n);
}
function an(e, t, n) {
	return jn(e).observe_(t, n);
}
function on(e, t, n, r) {
	return jn(e, t).observe_(n, r);
}
function sn() {
	if (process.env.NODE_ENV !== "production") {
		var e = !1, n = [...arguments];
		typeof n[n.length - 1] == "boolean" && (e = n.pop());
		var r = cn(n);
		if (!r) return t("'trace(break?)' can only be used inside a tracked computed value or a Reaction. Consider passing in the computed value or reaction explicitly");
		r.isTracing_ === U.NONE && console.log("[mobx.trace] '" + r.name_ + "' tracing enabled"), r.isTracing_ = e ? U.BREAK : U.LOG;
	}
}
function cn(e) {
	switch (e.length) {
		case 0: return W.trackingDerivation;
		case 1: return An(e[0]);
		case 2: return An(e[0], e[1]);
	}
}
function O(e, t) {
	t === void 0 && (t = void 0), C();
	try {
		return e.apply(t);
	} finally {
		w();
	}
}
function ln(e) {
	return e[R];
}
function un(e, t) {
	var n;
	return r(), e = bn(e, t), (n = e[R]).proxy_ ?? (n.proxy_ = new Proxy(e, Zr));
}
function k(e) {
	return e.interceptors_ !== void 0 && e.interceptors_.length > 0;
}
function dn(e, t) {
	var n = e.interceptors_ ||= [];
	return n.push(t), o(function() {
		var e = n.indexOf(t);
		e !== -1 && n.splice(e, 1);
	});
}
function A(e, n) {
	var r = bt();
	try {
		for (var i = [].concat(e.interceptors_ || []), a = 0, o = i.length; a < o && (n = i[a](n), n && !n.type && t(14), n); a++);
		return n;
	} finally {
		S(r);
	}
}
function j(e) {
	return e.changeListeners_ !== void 0 && e.changeListeners_.length > 0;
}
function fn(e, t) {
	var n = e.changeListeners_ ||= [];
	return n.push(t), o(function() {
		var e = n.indexOf(t);
		e !== -1 && n.splice(e, 1);
	});
}
function M(e, t) {
	var n = bt(), r = e.changeListeners_;
	if (r) {
		r = r.slice();
		for (var i = 0, a = r.length; i < a; i++) r[i](t);
		S(n);
	}
}
function pn(e, n, r) {
	return process.env.NODE_ENV !== "production" && (!u(e) && !u(Object.getPrototypeOf(e)) && t("'makeAutoObservable' can only be used for classes that don't have a superclass"), Sn(e) && t("makeAutoObservable can only be used on objects not already made observable")), u(e) ? Jt(e, e, n, r) : (Nn(function() {
		var t = bn(e, r)[R];
		if (!e[Qr]) {
			var i = Object.getPrototypeOf(e), a = new Set([].concat(Zn(e), Zn(i)));
			a.delete("constructor"), a.delete(R), f(i, Qr, a);
		}
		e[Qr].forEach(function(e) {
			return t.make_(e, n && e in n ? n[e] : !0);
		});
	}), e);
}
function mn(e, t, n, i) {
	return n === void 0 && (n = process.env.NODE_ENV === "production" ? "ObservableArray" : "ObservableArray@" + a()), i === void 0 && (i = !1), r(), Nn(function() {
		var r = new ni(n, t, i, !1);
		ee(r.values_, R, r);
		var a = new Proxy(r.values_, ti);
		return r.proxy_ = a, e && e.length && r.spliceWithArray_(0, 0, e), a;
	});
}
function N(e, t) {
	typeof Array.prototype[e] == "function" && (ri[e] = t(e));
}
function P(e) {
	return function() {
		var t = this[R];
		t.atom_.reportObserved();
		var n = t.dehanceValues_(t.values_);
		return n[e].apply(n, arguments);
	};
}
function F(e) {
	return function(t, n) {
		var r = this, i = this[R];
		return i.atom_.reportObserved(), i.dehanceValues_(i.values_)[e](function(e, i) {
			return t.call(n, e, i, r);
		});
	};
}
function hn(e) {
	return function() {
		var t = this, n = this[R];
		n.atom_.reportObserved();
		var r = n.dehanceValues_(n.values_), i = arguments[0];
		return arguments[0] = function(e, n, r) {
			return i(e, n, r, t);
		}, r[e].apply(r, arguments);
	};
}
function gn(e) {
	return l(e) && ii(e[R]);
}
function _n(e) {
	return e[Symbol.toStringTag] = "MapIterator", Ln(e);
}
function vn(e) {
	if (m(e) || Y(e)) return e;
	if (Array.isArray(e)) return new Map(e);
	if (u(e)) {
		var n = /* @__PURE__ */ new Map();
		for (var r in e) n.set(r, e[r]);
		return n;
	} else return t(21, e);
}
function yn(e) {
	return e[Symbol.toStringTag] = "SetIterator", Ln(e);
}
function bn(e, n) {
	if (process.env.NODE_ENV !== "production" && n && Sn(e) && t("Options can't be provided for already observable objects."), _(e, R)) return process.env.NODE_ENV !== "production" && !(jn(e) instanceof fi) && t("Cannot convert '" + Mn(e) + "' into observable object:\nThe target is already observable of different type.\nExtending builtins is not supported."), e;
	process.env.NODE_ENV !== "production" && !Object.isExtensible(e) && t("Cannot make the designated object observable; it is not extensible");
	var r = n?.name ?? (process.env.NODE_ENV === "production" ? "ObservableObject" : (u(e) ? "ObservableObject" : e.constructor.name) + "@" + a());
	return f(e, R, new fi(e, /* @__PURE__ */ new Map(), String(r), nt(n))), e;
}
function xn(e) {
	return ui[e] || (ui[e] = {
		get: function() {
			return this[R].getObservablePropValue_(e);
		},
		set: function(t) {
			return this[R].setObservablePropValue_(e, t);
		}
	});
}
function Sn(e) {
	return l(e) ? pi(e[R]) : !1;
}
function Cn(e, t, n) {
	var r;
	process.env.NODE_ENV !== "production" && (e.appliedAnnotations_[n] = t), (r = e.target_[L]) == null || delete r[n];
}
function wn(e, n, r) {
	if (process.env.NODE_ENV !== "production" && !zn(n) && t("Cannot annotate '" + e.name_ + "." + r.toString() + "': Invalid annotation."), process.env.NODE_ENV !== "production" && !De(n) && _(e.appliedAnnotations_, r)) {
		var i = e.name_ + "." + r.toString(), a = e.appliedAnnotations_[r].annotationType_, o = n.annotationType_;
		t("Cannot apply '" + o + "' to '" + i + "':" + ("\nThe field is already annotated with '" + a + "'.") + "\nRe-annotating fields is not allowed.\nUse 'override' annotation for methods overridden by subclass.");
	}
}
function Tn(e, t) {
	Object.setPrototypeOf ? Object.setPrototypeOf(e.prototype, t) : e.prototype.__proto__ === void 0 ? e.prototype = t : e.prototype.__proto__ = t;
}
function En(e) {
	return {
		enumerable: !1,
		configurable: !0,
		get: function() {
			return this[R].get_(e);
		},
		set: function(t) {
			this[R].set_(e, t);
		}
	};
}
function Dn(e) {
	I(vi.prototype, "" + e, En(e));
}
function On(e) {
	if (e > gi) {
		for (var t = gi; t < e + 100; t++) Dn(t);
		gi = e;
	}
}
function kn(e, t, n) {
	return new vi(e, t, n);
}
function An(e, n) {
	if (typeof e == "object" && e) {
		if (gn(e)) return n !== void 0 && t(23), e[R].atom_;
		if (X(e)) return e.atom_;
		if (Y(e)) {
			if (n === void 0) return e.keysAtom_;
			var r = e.data_.get(n) || e.hasMap_.get(n);
			return r || t(25, n, Mn(e)), r;
		}
		if (Sn(e)) {
			if (!n) return t(26);
			var i = e[R].values_.get(n);
			return i || t(27, n, Mn(e)), i;
		}
		if ($n(e) || Tr(e) || Mr(e)) return e;
	} else if (s(e) && Mr(e[R])) return e[R];
	t(28);
}
function jn(e, n) {
	if (e || t(29), n !== void 0) return jn(An(e, n));
	if ($n(e) || Tr(e) || Mr(e) || Y(e) || X(e)) return e;
	if (e[R]) return e[R];
	t(24, e);
}
function Mn(e, t) {
	var n;
	if (t !== void 0) n = An(e, t);
	else if (Bt(e)) return e.name;
	else n = Sn(e) || Y(e) || X(e) ? jn(e) : An(e);
	return n.name_;
}
function Nn(e) {
	var t = bt(), n = ut(!0);
	C();
	try {
		return e();
	} finally {
		w(), dt(n), S(t);
	}
}
function Pn(e, t, n) {
	return n === void 0 && (n = -1), Fn(e, t, n);
}
function Fn(e, t, n, r, i) {
	if (e === t) return e !== 0 || 1 / e == 1 / t;
	if (e == null || t == null) return !1;
	if (e !== e) return t !== t;
	var a = typeof e;
	if (a !== "function" && a !== "object" && typeof t != "object") return !1;
	var o = yi.call(e);
	if (o !== yi.call(t)) return !1;
	switch (o) {
		case "[object RegExp]":
		case "[object String]": return "" + e == "" + t;
		case "[object Number]": return +e == +e ? +e == 0 ? 1 / e == 1 / t : +e == +t : +t != +t;
		case "[object Date]":
		case "[object Boolean]": return +e == +t;
		case "[object Symbol]": return typeof Symbol < "u" && Symbol.valueOf.call(e) === Symbol.valueOf.call(t);
		case "[object Map]":
		case "[object Set]":
			n >= 0 && n++;
			break;
	}
	e = In(e), t = In(t);
	var c = o === "[object Array]";
	if (!c) {
		if (typeof e != "object" || typeof t != "object") return !1;
		var l = e.constructor, u = t.constructor;
		if (l !== u && !(s(l) && l instanceof l && s(u) && u instanceof u) && "constructor" in e && "constructor" in t) return !1;
	}
	if (n === 0) return !1;
	n < 0 && (n = -1), r ||= [], i ||= [];
	for (var d = r.length; d--;) if (r[d] === e) return i[d] === t;
	if (r.push(e), i.push(t), c) {
		if (d = e.length, d !== t.length) return !1;
		for (; d--;) if (!Fn(e[d], t[d], n - 1, r, i)) return !1;
	} else {
		var f = Object.keys(e), ee = f.length;
		if (Object.keys(t).length !== ee) return !1;
		for (var p = 0; p < ee; p++) {
			var m = f[p];
			if (!(_(t, m) && Fn(e[m], t[m], n - 1, r, i))) return !1;
		}
	}
	return r.pop(), i.pop(), !0;
}
function In(e) {
	return gn(e) ? e.slice() : m(e) || Y(e) || g(e) || X(e) ? Array.from(e.entries()) : e;
}
function Ln(e) {
	return e[Symbol.iterator] = Rn, Object.assign(Object.create(bi), e);
}
function Rn() {
	return this;
}
function zn(e) {
	return e instanceof Object && typeof e.annotationType_ == "string" && s(e.make_) && s(e.extend_);
}
var Bn, Vn, Hn, Un, I, Wn, Gn, Kn, qn, Jn, Yn, Xn, Zn, Qn, L, R, z, $n, er, tr, nr, rr, ir, ar, or, sr, cr, lr, ur, dr, fr, pr, B, mr, hr, gr, _r, vr, yr, br, xr, Sr, Cr, wr, V, Tr, H, U, Er, Dr, Or, kr, W, G, Ar, jr, Mr, Nr, Pr, Fr, Ir, Lr, Rr, zr, Br, Vr, Hr, K, Ur, Wr, Gr, Kr, qr, Jr, Yr, Xr, Zr, Qr, $r, q, ei, ti, ni, ri, ii, ai, J, oi, si, Y, ci, li, X, ui, di, fi, pi, mi, hi, gi, _i, vi, yi, bi, xi = e((() => {
	Bn = process.env.NODE_ENV === "production" ? {} : {
		0: "Invalid value for configuration 'enforceActions', expected 'never', 'always' or 'observed'",
		1: function(e, t) {
			return "Cannot apply '" + e + "' to '" + t.toString() + "': Field not found.";
		},
		5: "'keys()' can only be used on observable objects, arrays, sets and maps",
		6: "'values()' can only be used on observable objects, arrays, sets and maps",
		7: "'entries()' can only be used on observable objects, arrays and maps",
		8: "'set()' can only be used on observable objects, arrays and maps",
		9: "'remove()' can only be used on observable objects, arrays and maps",
		10: "'has()' can only be used on observable objects, arrays and maps",
		11: "'get()' can only be used on observable objects, arrays and maps",
		12: "Invalid annotation",
		13: "Dynamic observable objects cannot be frozen. If you're passing observables to 3rd party component/function that calls Object.freeze, pass copy instead: toJS(observable)",
		14: "Intercept handlers should return nothing or a change object",
		15: "Observable arrays cannot be frozen. If you're passing observables to 3rd party component/function that calls Object.freeze, pass copy instead: toJS(observable)",
		16: "Modification exception: the internal structure of an observable array was changed.",
		17: function(e, t) {
			return "[mobx.array] Index out of bounds, " + e + " is larger than " + t;
		},
		18: "mobx.map requires Map polyfill for the current browser. Check babel-polyfill or core-js/es6/map.js",
		19: function(e) {
			return "Cannot initialize from classes that inherit from Map: " + e.constructor.name;
		},
		20: function(e) {
			return "Cannot initialize map from " + e;
		},
		21: function(e) {
			return "Cannot convert to map from '" + e + "'";
		},
		22: "mobx.set requires Set polyfill for the current browser. Check babel-polyfill or core-js/es6/set.js",
		23: "It is not possible to get index atoms from arrays",
		24: function(e) {
			return "Cannot obtain administration from " + e;
		},
		25: function(e, t) {
			return "the entry '" + e + "' does not exist in the observable map '" + t + "'";
		},
		26: "please specify a property",
		27: function(e, t) {
			return "no observable property '" + e.toString() + "' found on the observable object '" + t + "'";
		},
		28: function(e) {
			return "Cannot obtain atom from " + e;
		},
		29: "Expecting some object",
		30: "invalid action stack. did you forget to finish an action?",
		31: "missing option for computed: get",
		32: function(e, t) {
			return "Cycle detected in computation " + e + ": " + t;
		},
		33: function(e) {
			return "The setter of computed value '" + e + "' is trying to update itself. Did you intend to update an _observable_ value, instead of the computed property?";
		},
		34: function(e) {
			return "[ComputedValue '" + e + "'] It is not possible to assign a new value to a computed value.";
		},
		35: "There are multiple, different versions of MobX active. Make sure MobX is loaded only once or use `configure({ isolateGlobalState: true })`",
		36: "isolateGlobalState should be called before MobX is running any reactions",
		37: function(e) {
			return "[mobx] `observableArray." + e + "()` mutates the array in-place, which is not allowed inside a derivation. Use `array.slice()." + e + "()` instead";
		},
		38: "'ownKeys()' can only be used on observable objects",
		39: "'defineProperty()' can only be used on observable objects"
	}, Vn = {}, Hn = Object.assign, Un = Object.getOwnPropertyDescriptor, I = Object.defineProperty, Wn = Object.prototype, Gn = [], Object.freeze(Gn), Kn = {}, Object.freeze(Kn), qn = typeof Proxy < "u", Jn = /* @__PURE__ */ Object.toString(), Yn = function() {}, Xn = Object.getOwnPropertySymbols !== void 0, Zn = typeof Reflect < "u" && Reflect.ownKeys ? Reflect.ownKeys : Xn ? function(e) {
		return Object.getOwnPropertyNames(e).concat(Object.getOwnPropertySymbols(e));
	} : 	/* istanbul ignore next */ Object.getOwnPropertyNames, Qn = Object.getOwnPropertyDescriptors || function(e) {
		var t = {};
		return Zn(e).forEach(function(n) {
			t[n] = Un(e, n);
		}), t;
	}, L = /* @__PURE__ */ Symbol("mobx-stored-annotations"), R = /* @__PURE__ */ Symbol("mobx administration"), z = /* @__PURE__ */ function() {
		function e(e) {
			e === void 0 && (e = process.env.NODE_ENV === "production" ? "Atom" : "Atom@" + a()), this.name_ = void 0, this.flags_ = 0, this.observers_ = /* @__PURE__ */ new Set(), this.lastAccessedBy_ = 0, this.lowestObserverState_ = H.NOT_TRACKING_, this.onBOL = void 0, this.onBUOL = void 0, this.name_ = e;
		}
		var t = e.prototype;
		return t.onBO = function() {
			this.onBOL && this.onBOL.forEach(function(e) {
				return e();
			});
		}, t.onBUO = function() {
			this.onBUOL && this.onBUOL.forEach(function(e) {
				return e();
			});
		}, t.reportObserved = function() {
			return Dt(this);
		}, t.reportChanged = function() {
			C(), Ot(this), w();
		}, t.toString = function() {
			return this.name_;
		}, oe(e, [
			{
				key: "isBeingObserved",
				get: function() {
					return v(this.flags_, e.isBeingObservedMask_);
				},
				set: function(t) {
					this.flags_ = y(this.flags_, e.isBeingObservedMask_, t);
				}
			},
			{
				key: "isPendingUnobservation",
				get: function() {
					return v(this.flags_, e.isPendingUnobservationMask_);
				},
				set: function(t) {
					this.flags_ = y(this.flags_, e.isPendingUnobservationMask_, t);
				}
			},
			{
				key: "diffValue",
				get: function() {
					return +!!v(this.flags_, e.diffValueMask_);
				},
				set: function(t) {
					this.flags_ = y(this.flags_, e.diffValueMask_, t === 1);
				}
			}
		]);
	}(), z.isBeingObservedMask_ = 1, z.isPendingUnobservationMask_ = 2, z.diffValueMask_ = 4, $n = /* @__PURE__ */ p("Atom", z), er = {
		identity: ye,
		structural: be,
		default: Se,
		shallow: xe
	}, tr = "override", nr = "true", rr = /* @__PURE__ */ Xe(), ir = "observable", ar = "observable.ref", or = "observable.shallow", sr = "observable.struct", cr = {
		deep: !0,
		name: void 0,
		defaultDecorator: void 0,
		proxy: !0
	}, Object.freeze(cr), lr = /* @__PURE__ */ Ge(ir), ur = /* @__PURE__ */ Ge(ar, { enhancer: Te }), dr = /* @__PURE__ */ Ge(or, { enhancer: we }), fr = /* @__PURE__ */ Ge(sr, { enhancer: Ee }), pr = /* @__PURE__ */ b(lr), Hn(it, pr), B = /* @__PURE__ */ Hn(it, {
		box: function(e, t) {
			var n = et(t);
			return new wr(e, tt(n), n.name, !0, n.equals);
		},
		array: function(e, t) {
			var n = et(t);
			return (W.useProxies === !1 || n.proxy === !1 ? kn : mn)(e, tt(n), n.name);
		},
		map: function(e, t) {
			var n = et(t);
			return new si(e, tt(n), n.name);
		},
		set: function(e, t) {
			var n = et(t);
			return new li(e, tt(n), n.name);
		},
		object: function(e, t, n) {
			return Nn(function() {
				return Jt(W.useProxies === !1 || n?.proxy === !1 ? bn({}, n) : un({}, n), e, t);
			});
		},
		ref: /* @__PURE__ */ b(ur),
		shallow: /* @__PURE__ */ b(dr),
		deep: pr,
		struct: /* @__PURE__ */ b(fr)
	}), mr = "computed", hr = "computed.struct", gr = /* @__PURE__ */ Be(mr), _r = /* @__PURE__ */ Be(hr, { equals: er.structural }), vr = function(e, n) {
		if (ge(n)) return gr.decorate_20223_(e, n);
		if (c(n)) return me(e, n, gr);
		if (u(e)) return b(Be(mr, e));
		process.env.NODE_ENV !== "production" && (s(e) || t("First argument to `computed` should be an expression."), s(n) && t("A setter as second argument is no longer supported, use `{ set: fn }` option instead"));
		var r = u(n) ? n : {};
		return r.get = e, r.name ||= e.name || "", new V(r);
	}, Object.assign(vr, gr), vr.struct = /* @__PURE__ */ b(_r), yr = 0, br = 1, xr = (/* @__PURE__ */ Un(function() {}, "name"))?.configurable ?? !1, Sr = {
		value: "action",
		configurable: !0,
		writable: !1,
		enumerable: !1
	}, Cr = "create", wr = /* @__PURE__ */ function(e) {
		function t(t, n, r, i, o) {
			var s;
			return r === void 0 && (r = process.env.NODE_ENV === "production" ? "ObservableValue" : "ObservableValue@" + a()), i === void 0 && (i = !0), o === void 0 && (o = er.default), s = e.call(this, r) || this, s.enhancer = void 0, s.name_ = void 0, s.equals = void 0, s.hasUnreportedChange_ = !1, s.interceptors_ = void 0, s.changeListeners_ = void 0, s.value_ = void 0, s.dehancer = void 0, s.enhancer = n, s.name_ = r, s.equals = o, s.value_ = n(t, void 0, r), process.env.NODE_ENV !== "production" && i && T() && It({
				type: Cr,
				object: s,
				observableKind: "value",
				debugObjectName: s.name_,
				newValue: "" + s.value_?.toString()
			}), s;
		}
		le(t, e);
		var n = t.prototype;
		return n.dehanceValue = function(e) {
			return this.dehancer === void 0 ? e : this.dehancer(e);
		}, n.set = function(e) {
			var t = this.value_;
			if (e = this.prepareNewValue_(e), e !== W.UNCHANGED) {
				var n = T();
				process.env.NODE_ENV !== "production" && n && E({
					type: q,
					object: this,
					observableKind: "value",
					debugObjectName: this.name_,
					newValue: e,
					oldValue: t
				}), this.setNewValue_(e), process.env.NODE_ENV !== "production" && n && D();
			}
		}, n.prepareNewValue_ = function(e) {
			if (x(this), k(this)) {
				var t = A(this, {
					object: this,
					type: q,
					newValue: e
				});
				if (!t) return W.UNCHANGED;
				e = t.newValue;
			}
			return e = this.enhancer(e, this.value_, this.name_), this.equals(this.value_, e) ? W.UNCHANGED : e;
		}, n.setNewValue_ = function(e) {
			var t = this.value_;
			this.value_ = e, this.reportChanged(), j(this) && M(this, {
				type: q,
				object: this,
				newValue: e,
				oldValue: t
			});
		}, n.get = function() {
			return this.reportObserved(), this.dehanceValue(this.value_);
		}, n.intercept_ = function(e) {
			return dn(this, e);
		}, n.observe_ = function(e, t) {
			return t && e({
				observableKind: "value",
				debugObjectName: this.name_,
				object: this,
				type: q,
				newValue: this.value_,
				oldValue: void 0
			}), fn(this, e);
		}, n.raw = function() {
			return this.value_;
		}, n.toJSON = function() {
			return this.get();
		}, n.toString = function() {
			return this.name_ + "[" + this.value_ + "]";
		}, n.valueOf = function() {
			return re(this.get());
		}, n[Symbol.toPrimitive] = function() {
			return this.valueOf();
		}, t;
	}(z), V = /* @__PURE__ */ function() {
		function e(e) {
			this.dependenciesState_ = H.NOT_TRACKING_, this.observing_ = [], this.newObserving_ = null, this.observers_ = /* @__PURE__ */ new Set(), this.runId_ = 0, this.lastAccessedBy_ = 0, this.lowestObserverState_ = H.UP_TO_DATE_, this.unboundDepsCount_ = 0, this.value_ = new Er(null), this.name_ = void 0, this.triggeredBy_ = void 0, this.flags_ = 0, this.derivation = void 0, this.setter_ = void 0, this.isTracing_ = U.NONE, this.scope_ = void 0, this.equals_ = void 0, this.requiresReaction_ = void 0, this.keepAlive_ = void 0, this.onBOL = void 0, this.onBUOL = void 0, e.get || t(31), this.derivation = e.get, this.name_ = e.name || (process.env.NODE_ENV === "production" ? "ComputedValue" : "ComputedValue@" + a()), e.set && (this.setter_ = at(process.env.NODE_ENV === "production" ? "ComputedValue-setter" : this.name_ + "-setter", e.set)), this.equals_ = e.equals || (e.compareStructural || e.struct ? er.structural : er.default), this.scope_ = e.context, this.requiresReaction_ = e.requiresReaction, this.keepAlive_ = !!e.keepAlive;
		}
		var n = e.prototype;
		return n.onBecomeStale_ = function() {
			At(this);
		}, n.onBO = function() {
			this.onBOL && this.onBOL.forEach(function(e) {
				return e();
			});
		}, n.onBUO = function() {
			this.onBUOL && this.onBUOL.forEach(function(e) {
				return e();
			});
		}, n.get = function() {
			if (this.isComputing && t(32, this.name_, this.derivation), W.inBatch === 0 && this.observers_.size === 0 && !this.keepAlive_) pt(this) && (this.warnAboutUntrackedRead_(), C(), this.value_ = this.computeValue_(!1), w());
			else if (Dt(this), pt(this)) {
				var e = W.trackingContext;
				this.keepAlive_ && !e && (W.trackingContext = this), this.trackAndCompute() && kt(this), W.trackingContext = e;
			}
			var n = this.value_;
			if (ft(n)) throw n.cause;
			return n;
		}, n.set = function(e) {
			if (this.setter_) {
				this.isRunningSetter && t(33, this.name_), this.isRunningSetter = !0;
				try {
					this.setter_.call(this.scope_, e);
				} finally {
					this.isRunningSetter = !1;
				}
			} else t(34, this.name_);
		}, n.trackAndCompute = function() {
			var e = this.value_, t = this.dependenciesState_ === H.NOT_TRACKING_, n = this.computeValue_(!0), r = t || ft(e) || ft(n) || !this.equals_(e, n);
			return r && (this.value_ = n, process.env.NODE_ENV !== "production" && T() && It({
				observableKind: "computed",
				debugObjectName: this.name_,
				object: this.scope_,
				type: "update",
				oldValue: e,
				newValue: n
			})), r;
		}, n.computeValue_ = function(e) {
			this.isComputing = !0;
			var t = ut(!1), n;
			if (e) n = ht(this, this.derivation, this.scope_);
			else if (W.disableErrorBoundaries === !0) n = this.derivation.call(this.scope_);
			else try {
				n = this.derivation.call(this.scope_);
			} catch (e) {
				n = new Er(e);
			}
			return dt(t), this.isComputing = !1, n;
		}, n.suspend_ = function() {
			this.keepAlive_ || (vt(this), this.value_ = void 0, process.env.NODE_ENV !== "production" && this.isTracing_ !== U.NONE && console.log("[mobx.trace] Computed value '" + this.name_ + "' was suspended and it will recompute on the next access."));
		}, n.observe_ = function(e, t) {
			var n = this, r = !0, i = void 0;
			return Vt(function() {
				var a = n.get();
				if (!r || t) {
					var o = bt();
					e({
						observableKind: "computed",
						debugObjectName: n.name_,
						type: q,
						object: n,
						newValue: a,
						oldValue: i
					}), S(o);
				}
				r = !1, i = a;
			});
		}, n.warnAboutUntrackedRead_ = function() {
			process.env.NODE_ENV !== "production" && (this.isTracing_ !== U.NONE && console.log("[mobx.trace] Computed value '" + this.name_ + "' is being read outside a reactive context. Doing a full recompute."), (typeof this.requiresReaction_ == "boolean" ? this.requiresReaction_ : W.computedRequiresReaction) && console.warn("[mobx] Computed value '" + this.name_ + "' is being read outside a reactive context. Doing a full recompute."));
		}, n.toString = function() {
			return this.name_ + "[" + this.derivation.toString() + "]";
		}, n.valueOf = function() {
			return re(this.get());
		}, n[Symbol.toPrimitive] = function() {
			return this.valueOf();
		}, oe(e, [
			{
				key: "isComputing",
				get: function() {
					return v(this.flags_, e.isComputingMask_);
				},
				set: function(t) {
					this.flags_ = y(this.flags_, e.isComputingMask_, t);
				}
			},
			{
				key: "isRunningSetter",
				get: function() {
					return v(this.flags_, e.isRunningSetterMask_);
				},
				set: function(t) {
					this.flags_ = y(this.flags_, e.isRunningSetterMask_, t);
				}
			},
			{
				key: "isBeingObserved",
				get: function() {
					return v(this.flags_, e.isBeingObservedMask_);
				},
				set: function(t) {
					this.flags_ = y(this.flags_, e.isBeingObservedMask_, t);
				}
			},
			{
				key: "isPendingUnobservation",
				get: function() {
					return v(this.flags_, e.isPendingUnobservationMask_);
				},
				set: function(t) {
					this.flags_ = y(this.flags_, e.isPendingUnobservationMask_, t);
				}
			},
			{
				key: "diffValue",
				get: function() {
					return +!!v(this.flags_, e.diffValueMask_);
				},
				set: function(t) {
					this.flags_ = y(this.flags_, e.diffValueMask_, t === 1);
				}
			}
		]);
	}(), V.isComputingMask_ = 1, V.isRunningSetterMask_ = 2, V.isBeingObservedMask_ = 4, V.isPendingUnobservationMask_ = 8, V.diffValueMask_ = 16, Tr = /* @__PURE__ */ p("ComputedValue", V), (function(e) {
		e[e.NOT_TRACKING_ = -1] = "NOT_TRACKING_", e[e.UP_TO_DATE_ = 0] = "UP_TO_DATE_", e[e.POSSIBLY_STALE_ = 1] = "POSSIBLY_STALE_", e[e.STALE_ = 2] = "STALE_";
	})(H ||= {}), (function(e) {
		e[e.NONE = 0] = "NONE", e[e.LOG = 1] = "LOG", e[e.BREAK = 2] = "BREAK";
	})(U ||= {}), Er = function(e) {
		this.cause = void 0, this.cause = e;
	}, Dr = function() {
		this.version = 6, this.UNCHANGED = {}, this.trackingDerivation = null, this.trackingContext = null, this.runId = 0, this.mobxGuid = 0, this.inBatch = 0, this.pendingUnobservations = [], this.pendingReactions = [], this.isRunningReactions = !1, this.allowStateChanges = !1, this.allowStateReads = !0, this.enforceActions = !0, this.spyListeners = [], this.globalReactionErrorHandlers = [], this.computedRequiresReaction = !1, this.reactionRequiresObservable = !1, this.observableRequiresReaction = !1, this.disableErrorBoundaries = !1, this.suppressReactionErrors = !1, this.useProxies = !0, this.verifyProxies = !1, this.safeDescriptors = !0;
	}, Or = !0, kr = !1, W = /* @__PURE__ */ function() {
		var e = /* @__PURE__ */ n();
		return e.__mobxInstanceCount > 0 && !e.__mobxGlobals && (Or = !1), e.__mobxGlobals && e.__mobxGlobals.version !== new Dr().version && (Or = !1), Or ? e.__mobxGlobals ? (e.__mobxInstanceCount += 1, e.__mobxGlobals.UNCHANGED || (e.__mobxGlobals.UNCHANGED = {}), e.__mobxGlobals) : (e.__mobxInstanceCount = 1, e.__mobxGlobals = /* @__PURE__ */ new Dr()) : (setTimeout(function() {
			kr || t(35);
		}, 1), new Dr());
	}(), G = /* @__PURE__ */ function() {
		function e(e, t, n, r) {
			e === void 0 && (e = process.env.NODE_ENV === "production" ? "Reaction" : "Reaction@" + a()), this.name_ = void 0, this.onInvalidate_ = void 0, this.errorHandler_ = void 0, this.requiresObservable_ = void 0, this.observing_ = [], this.newObserving_ = [], this.dependenciesState_ = H.NOT_TRACKING_, this.runId_ = 0, this.unboundDepsCount_ = 0, this.flags_ = 0, this.isTracing_ = U.NONE, this.name_ = e, this.onInvalidate_ = t, this.errorHandler_ = n, this.requiresObservable_ = r;
		}
		var t = e.prototype;
		return t.onBecomeStale_ = function() {
			this.schedule_();
		}, t.schedule_ = function() {
			this.isScheduled || (this.isScheduled = !0, W.pendingReactions.push(this), Pt());
		}, t.runReaction_ = function() {
			if (!this.isDisposed) {
				C(), this.isScheduled = !1;
				var e = W.trackingContext;
				if (W.trackingContext = this, pt(this)) {
					this.isTrackPending = !0;
					try {
						this.onInvalidate_(), process.env.NODE_ENV !== "production" && this.isTrackPending && T() && It({
							name: this.name_,
							type: "scheduled-reaction"
						});
					} catch (e) {
						this.reportExceptionInDerivation_(e);
					}
				}
				W.trackingContext = e, w();
			}
		}, t.track = function(e) {
			if (!this.isDisposed) {
				C();
				var t = T(), n;
				process.env.NODE_ENV !== "production" && t && (n = Date.now(), E({
					name: this.name_,
					type: "reaction"
				})), this.isRunning = !0;
				var r = W.trackingContext;
				W.trackingContext = this;
				var i = ht(this, e, void 0);
				W.trackingContext = r, this.isRunning = !1, this.isTrackPending = !1, this.isDisposed && vt(this), ft(i) && this.reportExceptionInDerivation_(i.cause), process.env.NODE_ENV !== "production" && t && D({ time: Date.now() - n }), w();
			}
		}, t.reportExceptionInDerivation_ = function(e) {
			var t = this;
			if (this.errorHandler_) {
				this.errorHandler_(e, this);
				return;
			}
			if (W.disableErrorBoundaries) throw e;
			var n = process.env.NODE_ENV === "production" ? "[mobx] uncaught error in '" + this + "'" : "[mobx] Encountered an uncaught exception that was thrown by a reaction or observer component, in: '" + this + "'";
			W.suppressReactionErrors ? process.env.NODE_ENV !== "production" && console.warn("[mobx] (error in reaction '" + this.name_ + "' suppressed, fix error of causing action below)") : console.error(n, e), process.env.NODE_ENV !== "production" && T() && It({
				type: "error",
				name: this.name_,
				message: n,
				error: "" + e
			}), W.globalReactionErrorHandlers.forEach(function(n) {
				return n(e, t);
			});
		}, t.dispose = function() {
			this.isDisposed || (this.isDisposed = !0, this.isRunning || (C(), vt(this), w()));
		}, t.getDisposer_ = function(e) {
			var t = this, n = function n() {
				t.dispose(), e == null || e.removeEventListener == null || e.removeEventListener("abort", n);
			};
			return e == null || e.addEventListener == null || e.addEventListener("abort", n), n[R] = this, "dispose" in Symbol && typeof Symbol.dispose == "symbol" && (n[Symbol.dispose] = n), n;
		}, t.toString = function() {
			return "Reaction[" + this.name_ + "]";
		}, t.trace = function(e) {
			e === void 0 && (e = !1), sn(this, e);
		}, oe(e, [
			{
				key: "isDisposed",
				get: function() {
					return v(this.flags_, e.isDisposedMask_);
				},
				set: function(t) {
					this.flags_ = y(this.flags_, e.isDisposedMask_, t);
				}
			},
			{
				key: "isScheduled",
				get: function() {
					return v(this.flags_, e.isScheduledMask_);
				},
				set: function(t) {
					this.flags_ = y(this.flags_, e.isScheduledMask_, t);
				}
			},
			{
				key: "isTrackPending",
				get: function() {
					return v(this.flags_, e.isTrackPendingMask_);
				},
				set: function(t) {
					this.flags_ = y(this.flags_, e.isTrackPendingMask_, t);
				}
			},
			{
				key: "isRunning",
				get: function() {
					return v(this.flags_, e.isRunningMask_);
				},
				set: function(t) {
					this.flags_ = y(this.flags_, e.isRunningMask_, t);
				}
			},
			{
				key: "diffValue",
				get: function() {
					return +!!v(this.flags_, e.diffValueMask_);
				},
				set: function(t) {
					this.flags_ = y(this.flags_, e.diffValueMask_, t === 1);
				}
			}
		]);
	}(), G.isDisposedMask_ = 1, G.isScheduledMask_ = 2, G.isTrackPendingMask_ = 4, G.isRunningMask_ = 8, G.diffValueMask_ = 16, Ar = 100, jr = function(e) {
		return e();
	}, Mr = /* @__PURE__ */ p("Reaction", G), Nr = {
		type: "report-end",
		spyReportEnd: !0
	}, Pr = "action", Fr = "action.bound", Ir = "autoAction", Lr = "autoAction.bound", Rr = "<unnamed action>", zr = /* @__PURE__ */ Oe(Pr), Br = /* @__PURE__ */ Oe(Fr, { bound: !0 }), Vr = /* @__PURE__ */ Oe(Ir, { autoAction: !0 }), Hr = /* @__PURE__ */ Oe(Lr, {
		autoAction: !0,
		bound: !0
	}), K = /* @__PURE__ */ Rt(!1), Object.assign(K, zr), Ur = /* @__PURE__ */ Rt(!0), Object.assign(Ur, Vr), K.bound = /* @__PURE__ */ b(Br), Ur.bound = /* @__PURE__ */ b(Hr), Wr = function(e) {
		return e();
	}, Gr = "onBO", Kr = "onBUO", qr = 0, Qt.prototype = /* @__PURE__ */ Object.create(Error.prototype), Jr = /* @__PURE__ */ Pe("flow"), Yr = /* @__PURE__ */ Pe("flow.bound", { bound: !0 }), Xr = /* @__PURE__ */ Object.assign(function(e, n) {
		if (ge(n)) return Jr.decorate_20223_(e, n);
		if (c(n)) return me(e, n, Jr);
		process.env.NODE_ENV !== "production" && arguments.length !== 1 && t("Flow expects single argument with generator function");
		var r = e, i = r.name || "<unnamed flow>", a = function() {
			var e = this, t = arguments, n = ++qr, a = K(i + " - runid: " + n + " - init", r).apply(e, t), o, c = void 0, l = new Promise(function(e, t) {
				var r = 0;
				o = t;
				function l(e) {
					c = void 0;
					var o;
					try {
						o = K(i + " - runid: " + n + " - yield " + r++, a.next).call(a, e);
					} catch (e) {
						return t(e);
					}
					d(o);
				}
				function u(e) {
					c = void 0;
					var o;
					try {
						o = K(i + " - runid: " + n + " - yield " + r++, a.throw).call(a, e);
					} catch (e) {
						return t(e);
					}
					d(o);
				}
				function d(n) {
					if (s(n?.then)) {
						n.then(d, t);
						return;
					}
					return n.done ? e(n.value) : (c = Promise.resolve(n.value), c.then(l, u));
				}
				l(void 0);
			});
			return l.cancel = K(i + " - runid: " + n + " - cancel", function() {
				try {
					c && $t(c);
					var e = a.return(void 0), t = Promise.resolve(e.value);
					t.then(Yn, Yn), $t(t), o(new Qt());
				} catch (e) {
					o(e);
				}
			}), l;
		};
		return a.isMobXFlow = !0, a;
	}, Jr), Xr.bound = /* @__PURE__ */ b(Yr), Zr = {
		has: function(e, t) {
			return process.env.NODE_ENV !== "production" && W.trackingDerivation && i("detect new properties using the 'in' operator. Use 'has' from 'mobx' instead."), ln(e).has_(t);
		},
		get: function(e, t) {
			return ln(e).get_(t);
		},
		set: function(e, t, n) {
			return c(t) ? (process.env.NODE_ENV !== "production" && !ln(e).values_.has(t) && i("add a new observable property through direct assignment. Use 'set' from 'mobx' instead."), ln(e).set_(t, n, !0) ?? !0) : !1;
		},
		deleteProperty: function(e, t) {
			return process.env.NODE_ENV !== "production" && i("delete properties from an observable object. Use 'remove' from 'mobx' instead."), c(t) ? ln(e).delete_(t, !0) ?? !0 : !1;
		},
		defineProperty: function(e, t, n) {
			return process.env.NODE_ENV !== "production" && i("define property on an observable object. Use 'defineProperty' from 'mobx' instead."), ln(e).defineProperty_(t, n) ?? !0;
		},
		ownKeys: function(e) {
			return process.env.NODE_ENV !== "production" && W.trackingDerivation && i("iterate keys to detect added / removed properties. Use 'keys' from 'mobx' instead."), ln(e).ownKeys_();
		},
		preventExtensions: function(e) {
			t(13);
		}
	}, Qr = /* @__PURE__ */ Symbol("mobx-keys"), $r = "splice", q = "update", ei = 1e4, ti = {
		get: function(e, t) {
			var n = e[R];
			return t === R ? n : t === "length" ? n.getArrayLength_() : typeof t == "string" && !isNaN(t) ? n.get_(parseInt(t)) : _(ri, t) ? ri[t] : e[t];
		},
		set: function(e, t, n) {
			var r = e[R];
			return t === "length" && r.setArrayLength_(n), typeof t == "symbol" || isNaN(t) ? e[t] = n : r.set_(parseInt(t), n), !0;
		},
		preventExtensions: function() {
			t(15);
		}
	}, ni = /* @__PURE__ */ function() {
		function e(e, t, n, r) {
			e === void 0 && (e = process.env.NODE_ENV === "production" ? "ObservableArray" : "ObservableArray@" + a()), this.owned_ = void 0, this.legacyMode_ = void 0, this.atom_ = void 0, this.values_ = [], this.interceptors_ = void 0, this.changeListeners_ = void 0, this.enhancer_ = void 0, this.dehancer = void 0, this.proxy_ = void 0, this.lastKnownLength_ = 0, this.owned_ = n, this.legacyMode_ = r, this.atom_ = new z(e), this.enhancer_ = function(n, r) {
				return t(n, r, process.env.NODE_ENV === "production" ? "ObservableArray[..]" : e + "[..]");
			};
		}
		var n = e.prototype;
		return n.dehanceValue_ = function(e) {
			return this.dehancer === void 0 ? e : this.dehancer(e);
		}, n.dehanceValues_ = function(e) {
			return this.dehancer !== void 0 && e.length > 0 ? e.map(this.dehancer) : e;
		}, n.intercept_ = function(e) {
			return dn(this, e);
		}, n.observe_ = function(e, t) {
			return t === void 0 && (t = !1), t && e({
				observableKind: "array",
				object: this.proxy_,
				debugObjectName: this.atom_.name_,
				type: "splice",
				index: 0,
				added: this.values_.slice(),
				addedCount: this.values_.length,
				removed: [],
				removedCount: 0
			}), fn(this, e);
		}, n.getArrayLength_ = function() {
			return this.atom_.reportObserved(), this.values_.length;
		}, n.setArrayLength_ = function(e) {
			(typeof e != "number" || isNaN(e) || e < 0) && t("Out of range: " + e);
			var n = this.values_.length;
			if (e !== n) if (e > n) {
				for (var r = Array(e - n), i = 0; i < e - n; i++) r[i] = void 0;
				this.spliceWithArray_(n, 0, r);
			} else this.spliceWithArray_(e, n - e);
		}, n.updateArrayLength_ = function(e, n) {
			e !== this.lastKnownLength_ && t(16), this.lastKnownLength_ += n, this.legacyMode_ && n > 0 && On(e + n + 1);
		}, n.spliceWithArray_ = function(e, t, n) {
			var r = this;
			x(this.atom_);
			var i = this.values_.length;
			if (e === void 0 ? e = 0 : e > i ? e = i : e < 0 && (e = Math.max(0, i + e)), t = arguments.length === 1 ? i - e : t == null ? 0 : Math.max(0, Math.min(t, i - e)), n === void 0 && (n = Gn), k(this)) {
				var a = A(this, {
					object: this.proxy_,
					type: $r,
					index: e,
					removedCount: t,
					added: n
				});
				if (!a) return Gn;
				t = a.removedCount, n = a.added;
			}
			if (n = n.length === 0 ? n : n.map(function(e) {
				return r.enhancer_(e, void 0);
			}), this.legacyMode_ || process.env.NODE_ENV !== "production") {
				var o = n.length - t;
				this.updateArrayLength_(i, o);
			}
			var s = this.spliceItemsIntoValues_(e, t, n);
			return (t !== 0 || n.length !== 0) && this.notifyArraySplice_(e, n, s), this.dehanceValues_(s);
		}, n.spliceItemsIntoValues_ = function(e, t, n) {
			if (n.length < ei) {
				var r;
				return (r = this.values_).splice.apply(r, [e, t].concat(n));
			} else {
				var i = this.values_.slice(e, e + t), a = this.values_.slice(e + t);
				this.values_.length += n.length - t;
				for (var o = 0; o < n.length; o++) this.values_[e + o] = n[o];
				for (var s = 0; s < a.length; s++) this.values_[e + n.length + s] = a[s];
				return i;
			}
		}, n.notifyArrayChildUpdate_ = function(e, t, n) {
			var r = !this.owned_ && T(), i = j(this), a = i || r ? {
				observableKind: "array",
				object: this.proxy_,
				type: q,
				debugObjectName: this.atom_.name_,
				index: e,
				newValue: t,
				oldValue: n
			} : null;
			process.env.NODE_ENV !== "production" && r && E(a), this.atom_.reportChanged(), i && M(this, a), process.env.NODE_ENV !== "production" && r && D();
		}, n.notifyArraySplice_ = function(e, t, n) {
			var r = !this.owned_ && T(), i = j(this), a = i || r ? {
				observableKind: "array",
				object: this.proxy_,
				debugObjectName: this.atom_.name_,
				type: $r,
				index: e,
				removed: n,
				added: t,
				removedCount: n.length,
				addedCount: t.length
			} : null;
			process.env.NODE_ENV !== "production" && r && E(a), this.atom_.reportChanged(), i && M(this, a), process.env.NODE_ENV !== "production" && r && D();
		}, n.get_ = function(e) {
			if (this.legacyMode_ && e >= this.values_.length) {
				console.warn(process.env.NODE_ENV === "production" ? "[mobx] Out of bounds read: " + e : "[mobx.array] Attempt to read an array index (" + e + ") that is out of bounds (" + this.values_.length + "). Please check length first. Out of bound indices will not be tracked by MobX");
				return;
			}
			return this.atom_.reportObserved(), this.dehanceValue_(this.values_[e]);
		}, n.set_ = function(e, n) {
			var r = this.values_;
			if (this.legacyMode_ && e > r.length && t(17, e, r.length), e < r.length) {
				x(this.atom_);
				var i = r[e];
				if (k(this)) {
					var a = A(this, {
						type: q,
						object: this.proxy_,
						index: e,
						newValue: n
					});
					if (!a) return;
					n = a.newValue;
				}
				n = this.enhancer_(n, i), n !== i && (r[e] = n, this.notifyArrayChildUpdate_(e, n, i));
			} else {
				for (var o = Array(e + 1 - r.length), s = 0; s < o.length - 1; s++) o[s] = void 0;
				o[o.length - 1] = n, this.spliceWithArray_(r.length, 0, o);
			}
		}, e;
	}(), ri = {
		clear: function() {
			return this.splice(0);
		},
		replace: function(e) {
			var t = this[R];
			return t.spliceWithArray_(0, t.values_.length, e);
		},
		toJSON: function() {
			return this.slice();
		},
		splice: function(e, t) {
			var n = [...arguments].slice(2), r = this[R];
			switch (arguments.length) {
				case 0: return [];
				case 1: return r.spliceWithArray_(e);
				case 2: return r.spliceWithArray_(e, t);
			}
			return r.spliceWithArray_(e, t, n);
		},
		spliceWithArray: function(e, t, n) {
			return this[R].spliceWithArray_(e, t, n);
		},
		push: function() {
			var e = this[R], t = [...arguments];
			return e.spliceWithArray_(e.values_.length, 0, t), e.values_.length;
		},
		pop: function() {
			return this.splice(Math.max(this[R].values_.length - 1, 0), 1)[0];
		},
		shift: function() {
			return this.splice(0, 1)[0];
		},
		unshift: function() {
			var e = this[R], t = [...arguments];
			return e.spliceWithArray_(0, 0, t), e.values_.length;
		},
		reverse: function() {
			return W.trackingDerivation && t(37, "reverse"), this.replace(this.slice().reverse()), this;
		},
		sort: function() {
			W.trackingDerivation && t(37, "sort");
			var e = this.slice();
			return e.sort.apply(e, arguments), this.replace(e), this;
		},
		remove: function(e) {
			var t = this[R], n = t.dehanceValues_(t.values_).indexOf(e);
			return n > -1 ? (this.splice(n, 1), !0) : !1;
		}
	}, N("at", P), N("concat", P), N("flat", P), N("includes", P), N("indexOf", P), N("join", P), N("lastIndexOf", P), N("slice", P), N("toString", P), N("toLocaleString", P), N("toSorted", P), N("toSpliced", P), N("with", P), N("every", F), N("filter", F), N("find", F), N("findIndex", F), N("findLast", F), N("findLastIndex", F), N("flatMap", F), N("forEach", F), N("map", F), N("some", F), N("toReversed", F), N("reduce", hn), N("reduceRight", hn), ii = /* @__PURE__ */ p("ObservableArrayAdministration", ni), ai = {}, J = "add", oi = "delete", si = /* @__PURE__ */ function() {
		function e(e, n, r) {
			var i = this;
			n === void 0 && (n = Ce), r === void 0 && (r = process.env.NODE_ENV === "production" ? "ObservableMap" : "ObservableMap@" + a()), this.enhancer_ = void 0, this.name_ = void 0, this[R] = ai, this.data_ = void 0, this.hasMap_ = void 0, this.keysAtom_ = void 0, this.interceptors_ = void 0, this.changeListeners_ = void 0, this.dehancer = void 0, this.enhancer_ = n, this.name_ = r, s(Map) || t(18), Nn(function() {
				i.keysAtom_ = ve(process.env.NODE_ENV === "production" ? "ObservableMap.keys()" : i.name_ + ".keys()"), i.data_ = /* @__PURE__ */ new Map(), i.hasMap_ = /* @__PURE__ */ new Map(), e && i.merge(e);
			});
		}
		var n = e.prototype;
		return n.has_ = function(e) {
			return this.data_.has(e);
		}, n.has = function(e) {
			var t = this;
			if (!W.trackingDerivation) return this.has_(e);
			var n = this.hasMap_.get(e);
			if (!n) {
				var r = n = new wr(this.has_(e), Te, process.env.NODE_ENV === "production" ? "ObservableMap.key?" : this.name_ + "." + ne(e) + "?", !1);
				this.hasMap_.set(e, r), Kt(r, function() {
					return t.hasMap_.delete(e);
				});
			}
			return n.get();
		}, n.set = function(e, t) {
			var n = this.has_(e);
			if (k(this)) {
				var r = A(this, {
					type: n ? q : J,
					object: this,
					newValue: t,
					name: e
				});
				if (!r) return this;
				t = r.newValue;
			}
			return n ? this.updateValue_(e, t) : this.addValue_(e, t), this;
		}, n.delete = function(e) {
			var t = this;
			if (x(this.keysAtom_), k(this) && !A(this, {
				type: oi,
				object: this,
				name: e
			})) return !1;
			if (this.has_(e)) {
				var n = T(), r = j(this), i = r || n ? {
					observableKind: "map",
					debugObjectName: this.name_,
					type: oi,
					object: this,
					oldValue: this.data_.get(e).value_,
					name: e
				} : null;
				return process.env.NODE_ENV !== "production" && n && E(i), O(function() {
					var n;
					t.keysAtom_.reportChanged(), (n = t.hasMap_.get(e)) == null || n.setNewValue_(!1), t.data_.get(e).setNewValue_(void 0), t.data_.delete(e);
				}), r && M(this, i), process.env.NODE_ENV !== "production" && n && D(), !0;
			}
			return !1;
		}, n.updateValue_ = function(e, t) {
			var n = this.data_.get(e);
			if (t = n.prepareNewValue_(t), t !== W.UNCHANGED) {
				var r = T(), i = j(this), a = i || r ? {
					observableKind: "map",
					debugObjectName: this.name_,
					type: q,
					object: this,
					oldValue: n.value_,
					name: e,
					newValue: t
				} : null;
				process.env.NODE_ENV !== "production" && r && E(a), n.setNewValue_(t), i && M(this, a), process.env.NODE_ENV !== "production" && r && D();
			}
		}, n.addValue_ = function(e, t) {
			var n = this;
			x(this.keysAtom_), O(function() {
				var r, i = new wr(t, n.enhancer_, process.env.NODE_ENV === "production" ? "ObservableMap.key" : n.name_ + "." + ne(e), !1);
				n.data_.set(e, i), t = i.value_, (r = n.hasMap_.get(e)) == null || r.setNewValue_(!0), n.keysAtom_.reportChanged();
			});
			var r = T(), i = j(this), a = i || r ? {
				observableKind: "map",
				debugObjectName: this.name_,
				type: J,
				object: this,
				name: e,
				newValue: t
			} : null;
			process.env.NODE_ENV !== "production" && r && E(a), i && M(this, a), process.env.NODE_ENV !== "production" && r && D();
		}, n.get = function(e) {
			return this.has(e) ? this.dehanceValue_(this.data_.get(e).get()) : this.dehanceValue_(void 0);
		}, n.dehanceValue_ = function(e) {
			return this.dehancer === void 0 ? e : this.dehancer(e);
		}, n.keys = function() {
			return this.keysAtom_.reportObserved(), this.data_.keys();
		}, n.values = function() {
			var e = this, t = this.keys();
			return _n({ next: function() {
				var n = t.next(), r = n.done, i = n.value;
				return {
					done: r,
					value: r ? void 0 : e.get(i)
				};
			} });
		}, n.entries = function() {
			var e = this, t = this.keys();
			return _n({ next: function() {
				var n = t.next(), r = n.done, i = n.value;
				return {
					done: r,
					value: r ? void 0 : [i, e.get(i)]
				};
			} });
		}, n[Symbol.iterator] = function() {
			return this.entries();
		}, n.forEach = function(e, t) {
			for (var n = se(this), r; !(r = n()).done;) {
				var i = r.value, a = i[0], o = i[1];
				e.call(t, o, a, this);
			}
		}, n.merge = function(e) {
			var n = this;
			return Y(e) && (e = new Map(e)), O(function() {
				u(e) ? te(e).forEach(function(t) {
					return n.set(t, e[t]);
				}) : Array.isArray(e) ? e.forEach(function(e) {
					var t = e[0], r = e[1];
					return n.set(t, r);
				}) : m(e) ? (h(e) || t(19, e), e.forEach(function(e, t) {
					return n.set(t, e);
				})) : e != null && t(20, e);
			}), this;
		}, n.clear = function() {
			var e = this;
			O(function() {
				yt(function() {
					for (var t = se(e.keys()), n; !(n = t()).done;) {
						var r = n.value;
						e.delete(r);
					}
				});
			});
		}, n.replace = function(e) {
			var t = this;
			return O(function() {
				for (var n = vn(e), r = /* @__PURE__ */ new Map(), i = !1, a = se(t.data_.keys()), o; !(o = a()).done;) {
					var s = o.value;
					if (!n.has(s)) if (t.delete(s)) i = !0;
					else {
						var c = t.data_.get(s);
						r.set(s, c);
					}
				}
				for (var l = se(n.entries()), u; !(u = l()).done;) {
					var d = u.value, f = d[0], ee = d[1], p = t.data_.has(f);
					if (t.set(f, ee), t.data_.has(f)) {
						var m = t.data_.get(f);
						r.set(f, m), p || (i = !0);
					}
				}
				if (!i) if (t.data_.size !== r.size) t.keysAtom_.reportChanged();
				else for (var h = t.data_.keys(), g = r.keys(), te = h.next(), ne = g.next(); !te.done;) {
					if (te.value !== ne.value) {
						t.keysAtom_.reportChanged();
						break;
					}
					te = h.next(), ne = g.next();
				}
				t.data_ = r;
			}), this;
		}, n.toString = function() {
			return "[object ObservableMap]";
		}, n.toJSON = function() {
			return Array.from(this);
		}, n.observe_ = function(e, n) {
			return process.env.NODE_ENV !== "production" && n === !0 && t("`observe` doesn't support fireImmediately=true in combination with maps."), fn(this, e);
		}, n.intercept_ = function(e) {
			return dn(this, e);
		}, oe(e, [{
			key: "size",
			get: function() {
				return this.keysAtom_.reportObserved(), this.data_.size;
			}
		}, {
			key: Symbol.toStringTag,
			get: function() {
				return "Map";
			}
		}]);
	}(), Y = /* @__PURE__ */ p("ObservableMap", si), ci = {}, li = /* @__PURE__ */ function() {
		function e(e, n, r) {
			var i = this;
			n === void 0 && (n = Ce), r === void 0 && (r = process.env.NODE_ENV === "production" ? "ObservableSet" : "ObservableSet@" + a()), this.name_ = void 0, this[R] = ci, this.data_ = /* @__PURE__ */ new Set(), this.atom_ = void 0, this.changeListeners_ = void 0, this.interceptors_ = void 0, this.dehancer = void 0, this.enhancer_ = void 0, this.name_ = r, s(Set) || t(22), this.enhancer_ = function(e, t) {
				return n(e, t, r);
			}, Nn(function() {
				i.atom_ = ve(i.name_), e && i.replace(e);
			});
		}
		var n = e.prototype;
		return n.dehanceValue_ = function(e) {
			return this.dehancer === void 0 ? e : this.dehancer(e);
		}, n.clear = function() {
			var e = this;
			O(function() {
				yt(function() {
					for (var t = se(e.data_.values()), n; !(n = t()).done;) {
						var r = n.value;
						e.delete(r);
					}
				});
			});
		}, n.forEach = function(e, t) {
			for (var n = se(this), r; !(r = n()).done;) {
				var i = r.value;
				e.call(t, i, i, this);
			}
		}, n.add = function(e) {
			var t = this;
			if (x(this.atom_), k(this)) {
				var n = A(this, {
					type: J,
					object: this,
					newValue: e
				});
				if (!n) return this;
				e = n.newValue;
			}
			if (!this.has(e)) {
				O(function() {
					t.data_.add(t.enhancer_(e, void 0)), t.atom_.reportChanged();
				});
				var r = process.env.NODE_ENV !== "production" && T(), i = j(this), a = i || r ? {
					observableKind: "set",
					debugObjectName: this.name_,
					type: J,
					object: this,
					newValue: e
				} : null;
				r && process.env.NODE_ENV !== "production" && E(a), i && M(this, a), r && process.env.NODE_ENV !== "production" && D();
			}
			return this;
		}, n.delete = function(e) {
			var t = this;
			if (k(this) && !A(this, {
				type: oi,
				object: this,
				oldValue: e
			})) return !1;
			if (this.has(e)) {
				var n = process.env.NODE_ENV !== "production" && T(), r = j(this), i = r || n ? {
					observableKind: "set",
					debugObjectName: this.name_,
					type: oi,
					object: this,
					oldValue: e
				} : null;
				return n && process.env.NODE_ENV !== "production" && E(i), O(function() {
					t.atom_.reportChanged(), t.data_.delete(e);
				}), r && M(this, i), n && process.env.NODE_ENV !== "production" && D(), !0;
			}
			return !1;
		}, n.has = function(e) {
			return this.atom_.reportObserved(), this.data_.has(this.dehanceValue_(e));
		}, n.entries = function() {
			var e = this.values();
			return yn({ next: function() {
				var t = e.next(), n = t.value, r = t.done;
				return r ? {
					value: void 0,
					done: r
				} : {
					value: [n, n],
					done: r
				};
			} });
		}, n.keys = function() {
			return this.values();
		}, n.values = function() {
			this.atom_.reportObserved();
			var e = this, t = this.data_.values();
			return yn({ next: function() {
				var n = t.next(), r = n.value, i = n.done;
				return i ? {
					value: void 0,
					done: i
				} : {
					value: e.dehanceValue_(r),
					done: i
				};
			} });
		}, n.intersection = function(e) {
			return g(e) && !X(e) ? e.intersection(this) : new Set(this).intersection(e);
		}, n.union = function(e) {
			return g(e) && !X(e) ? e.union(this) : new Set(this).union(e);
		}, n.difference = function(e) {
			return new Set(this).difference(e);
		}, n.symmetricDifference = function(e) {
			return g(e) && !X(e) ? e.symmetricDifference(this) : new Set(this).symmetricDifference(e);
		}, n.isSubsetOf = function(e) {
			return new Set(this).isSubsetOf(e);
		}, n.isSupersetOf = function(e) {
			return new Set(this).isSupersetOf(e);
		}, n.isDisjointFrom = function(e) {
			return g(e) && !X(e) ? e.isDisjointFrom(this) : new Set(this).isDisjointFrom(e);
		}, n.replace = function(e) {
			var n = this;
			return X(e) && (e = new Set(e)), O(function() {
				Array.isArray(e) || g(e) ? (n.clear(), e.forEach(function(e) {
					return n.add(e);
				})) : e != null && t("Cannot initialize set from " + e);
			}), this;
		}, n.observe_ = function(e, n) {
			return process.env.NODE_ENV !== "production" && n === !0 && t("`observe` doesn't support fireImmediately=true in combination with sets."), fn(this, e);
		}, n.intercept_ = function(e) {
			return dn(this, e);
		}, n.toJSON = function() {
			return Array.from(this);
		}, n.toString = function() {
			return "[object ObservableSet]";
		}, n[Symbol.iterator] = function() {
			return this.values();
		}, oe(e, [{
			key: "size",
			get: function() {
				return this.atom_.reportObserved(), this.data_.size;
			}
		}, {
			key: Symbol.toStringTag,
			get: function() {
				return "Set";
			}
		}]);
	}(), X = /* @__PURE__ */ p("ObservableSet", li), ui = /* @__PURE__ */ Object.create(null), di = "remove", fi = /* @__PURE__ */ function() {
		function e(e, n, r, i) {
			n === void 0 && (n = /* @__PURE__ */ new Map()), i === void 0 && (i = rr), this.target_ = void 0, this.values_ = void 0, this.name_ = void 0, this.defaultAnnotation_ = void 0, this.keysAtom_ = void 0, this.changeListeners_ = void 0, this.interceptors_ = void 0, this.proxy_ = void 0, this.isPlainObject_ = void 0, this.appliedAnnotations_ = void 0, this.pendingKeys_ = void 0, this.target_ = e, this.values_ = n, this.name_ = r, this.defaultAnnotation_ = i, this.keysAtom_ = new z(process.env.NODE_ENV === "production" ? "ObservableObject.keys" : this.name_ + ".keys"), this.isPlainObject_ = u(this.target_), process.env.NODE_ENV !== "production" && !zn(this.defaultAnnotation_) && t("defaultAnnotation must be valid annotation"), process.env.NODE_ENV !== "production" && (this.appliedAnnotations_ = {});
		}
		var n = e.prototype;
		return n.getObservablePropValue_ = function(e) {
			return this.values_.get(e).get();
		}, n.setObservablePropValue_ = function(e, t) {
			var n = this.values_.get(e);
			if (n instanceof V) return n.set(t), !0;
			if (k(this)) {
				var r = A(this, {
					type: q,
					object: this.proxy_ || this.target_,
					name: e,
					newValue: t
				});
				if (!r) return null;
				t = r.newValue;
			}
			if (t = n.prepareNewValue_(t), t !== W.UNCHANGED) {
				var i = j(this), a = process.env.NODE_ENV !== "production" && T(), o = i || a ? {
					type: q,
					observableKind: "object",
					debugObjectName: this.name_,
					object: this.proxy_ || this.target_,
					oldValue: n.value_,
					name: e,
					newValue: t
				} : null;
				process.env.NODE_ENV !== "production" && a && E(o), n.setNewValue_(t), i && M(this, o), process.env.NODE_ENV !== "production" && a && D();
			}
			return !0;
		}, n.get_ = function(e) {
			return W.trackingDerivation && !_(this.target_, e) && this.has_(e), this.target_[e];
		}, n.set_ = function(e, t, n) {
			return n === void 0 && (n = !1), _(this.target_, e) ? this.values_.has(e) ? this.setObservablePropValue_(e, t) : n ? Reflect.set(this.target_, e, t) : (this.target_[e] = t, !0) : this.extend_(e, {
				value: t,
				enumerable: !0,
				writable: !0,
				configurable: !0
			}, this.defaultAnnotation_, n);
		}, n.has_ = function(e) {
			if (!W.trackingDerivation) return e in this.target_;
			this.pendingKeys_ ||= /* @__PURE__ */ new Map();
			var t = this.pendingKeys_.get(e);
			return t || (t = new wr(e in this.target_, Te, process.env.NODE_ENV === "production" ? "ObservableObject.key?" : this.name_ + "." + ne(e) + "?", !1), this.pendingKeys_.set(e, t)), t.get();
		}, n.make_ = function(e, n) {
			if (n === !0 && (n = this.defaultAnnotation_), n !== !1) {
				if (wn(this, n, e), !(e in this.target_)) {
					var r;
					if ((r = this.target_[L]) != null && r[e]) return;
					t(1, n.annotationType_, this.name_ + "." + e.toString());
				}
				for (var i = this.target_; i && i !== Wn;) {
					var a = Un(i, e);
					if (a) {
						var o = n.make_(this, e, a, i);
						if (o === 0) return;
						if (o === 1) break;
					}
					i = Object.getPrototypeOf(i);
				}
				Cn(this, n, e);
			}
		}, n.extend_ = function(e, t, n, r) {
			if (r === void 0 && (r = !1), n === !0 && (n = this.defaultAnnotation_), n === !1) return this.defineProperty_(e, t, r);
			wn(this, n, e);
			var i = n.extend_(this, e, t, r);
			return i && Cn(this, n, e), i;
		}, n.defineProperty_ = function(e, t, n) {
			n === void 0 && (n = !1), x(this.keysAtom_);
			try {
				C();
				var r = this.delete_(e);
				if (!r) return r;
				if (k(this)) {
					var i = A(this, {
						object: this.proxy_ || this.target_,
						name: e,
						type: J,
						newValue: t.value
					});
					if (!i) return null;
					var a = i.newValue;
					t.value !== a && (t = ce({}, t, { value: a }));
				}
				if (n) {
					if (!Reflect.defineProperty(this.target_, e, t)) return !1;
				} else I(this.target_, e, t);
				this.notifyPropertyAddition_(e, t.value);
			} finally {
				w();
			}
			return !0;
		}, n.defineObservableProperty_ = function(e, t, n, r) {
			r === void 0 && (r = !1), x(this.keysAtom_);
			try {
				C();
				var i = this.delete_(e);
				if (!i) return i;
				if (k(this)) {
					var a = A(this, {
						object: this.proxy_ || this.target_,
						name: e,
						type: J,
						newValue: t
					});
					if (!a) return null;
					t = a.newValue;
				}
				var o = xn(e), s = {
					configurable: W.safeDescriptors ? this.isPlainObject_ : !0,
					enumerable: !0,
					get: o.get,
					set: o.set
				};
				if (r) {
					if (!Reflect.defineProperty(this.target_, e, s)) return !1;
				} else I(this.target_, e, s);
				var c = new wr(t, n, process.env.NODE_ENV === "production" ? "ObservableObject.key" : this.name_ + "." + e.toString(), !1);
				this.values_.set(e, c), this.notifyPropertyAddition_(e, c.value_);
			} finally {
				w();
			}
			return !0;
		}, n.defineComputedProperty_ = function(e, t, n) {
			n === void 0 && (n = !1), x(this.keysAtom_);
			try {
				C();
				var r = this.delete_(e);
				if (!r) return r;
				if (k(this) && !A(this, {
					object: this.proxy_ || this.target_,
					name: e,
					type: J,
					newValue: void 0
				})) return null;
				t.name ||= process.env.NODE_ENV === "production" ? "ObservableObject.key" : this.name_ + "." + e.toString(), t.context = this.proxy_ || this.target_;
				var i = xn(e), a = {
					configurable: W.safeDescriptors ? this.isPlainObject_ : !0,
					enumerable: !1,
					get: i.get,
					set: i.set
				};
				if (n) {
					if (!Reflect.defineProperty(this.target_, e, a)) return !1;
				} else I(this.target_, e, a);
				this.values_.set(e, new V(t)), this.notifyPropertyAddition_(e, void 0);
			} finally {
				w();
			}
			return !0;
		}, n.delete_ = function(e, t) {
			if (t === void 0 && (t = !1), x(this.keysAtom_), !_(this.target_, e)) return !0;
			if (k(this) && !A(this, {
				object: this.proxy_ || this.target_,
				name: e,
				type: di
			})) return null;
			try {
				var n;
				C();
				var r = j(this), i = process.env.NODE_ENV !== "production" && T(), a = this.values_.get(e), o = void 0;
				if (!a && (r || i) && (o = Un(this.target_, e)?.value), t) {
					if (!Reflect.deleteProperty(this.target_, e)) return !1;
				} else delete this.target_[e];
				if (process.env.NODE_ENV !== "production" && delete this.appliedAnnotations_[e], a && (this.values_.delete(e), a instanceof wr && (o = a.value_), Ot(a)), this.keysAtom_.reportChanged(), (n = this.pendingKeys_) == null || (n = n.get(e)) == null || n.set(e in this.target_), r || i) {
					var s = {
						type: di,
						observableKind: "object",
						object: this.proxy_ || this.target_,
						debugObjectName: this.name_,
						oldValue: o,
						name: e
					};
					process.env.NODE_ENV !== "production" && i && E(s), r && M(this, s), process.env.NODE_ENV !== "production" && i && D();
				}
			} finally {
				w();
			}
			return !0;
		}, n.observe_ = function(e, n) {
			return process.env.NODE_ENV !== "production" && n === !0 && t("`observe` doesn't support the fire immediately property for observable objects."), fn(this, e);
		}, n.intercept_ = function(e) {
			return dn(this, e);
		}, n.notifyPropertyAddition_ = function(e, t) {
			var n, r = j(this), i = process.env.NODE_ENV !== "production" && T();
			if (r || i) {
				var a = r || i ? {
					type: J,
					observableKind: "object",
					debugObjectName: this.name_,
					object: this.proxy_ || this.target_,
					name: e,
					newValue: t
				} : null;
				process.env.NODE_ENV !== "production" && i && E(a), r && M(this, a), process.env.NODE_ENV !== "production" && i && D();
			}
			(n = this.pendingKeys_) == null || (n = n.get(e)) == null || n.set(!0), this.keysAtom_.reportChanged();
		}, n.ownKeys_ = function() {
			return this.keysAtom_.reportObserved(), Zn(this.target_);
		}, n.keys_ = function() {
			return this.keysAtom_.reportObserved(), Object.keys(this.target_);
		}, e;
	}(), pi = /* @__PURE__ */ p("ObservableObjectAdministration", fi), mi = /* @__PURE__ */ En(0), hi = /* @__PURE__ */ function() {
		var e = !1, t = {};
		return Object.defineProperty(t, "0", { set: function() {
			e = !0;
		} }), Object.create(t)[0] = 1, e === !1;
	}(), gi = 0, _i = function() {}, Tn(_i, Array.prototype), vi = /* @__PURE__ */ function(e) {
		function t(t, n, r, i) {
			var o;
			return r === void 0 && (r = process.env.NODE_ENV === "production" ? "ObservableArray" : "ObservableArray@" + a()), i === void 0 && (i = !1), o = e.call(this) || this, Nn(function() {
				var e = new ni(r, n, i, !0);
				e.proxy_ = o, ee(o, R, e), t && t.length && o.spliceWithArray(0, 0, t), hi && Object.defineProperty(o, "0", mi);
			}), o;
		}
		le(t, e);
		var n = t.prototype;
		return n.concat = function() {
			this[R].atom_.reportObserved();
			var e = [...arguments];
			return Array.prototype.concat.apply(this.slice(), e.map(function(e) {
				return gn(e) ? e.slice() : e;
			}));
		}, n[Symbol.iterator] = function() {
			var e = this, t = 0;
			return Ln({ next: function() {
				return t < e.length ? {
					value: e[t++],
					done: !1
				} : {
					done: !0,
					value: void 0
				};
			} });
		}, oe(t, [{
			key: "length",
			get: function() {
				return this[R].getArrayLength_();
			},
			set: function(e) {
				this[R].setArrayLength_(e);
			}
		}, {
			key: Symbol.toStringTag,
			get: function() {
				return "Array";
			}
		}]);
	}(_i), Object.entries(ri).forEach(function(e) {
		var t = e[0], n = e[1];
		t !== "concat" && f(vi.prototype, t, n);
	}), On(1e3), yi = Wn.toString, bi = n().Iterator?.prototype || {}, [
		"Symbol",
		"Map",
		"Set"
	].forEach(function(e) {
		n()[e] === void 0 && t("MobX requires global '" + e + "' to be available or polyfilled");
	}), typeof __MOBX_DEVTOOLS_GLOBAL_HOOK__ == "object" && __MOBX_DEVTOOLS_GLOBAL_HOOK__.injectMobx({
		spy: Lt,
		extras: { getDebugName: Mn },
		$mobx: R
	});
}));
//#endregion
//#region frontend/copilot/shared/copilot-plugin-support.ts
function Si(e) {
	return Ti.has(e);
}
var Ci, wi, Ti, Ei = e((() => {
	Ci = /* @__PURE__ */ function(e) {
		return e.INFORMATION = "information", e.WARNING = "warning", e.ERROR = "error", e;
	}({}), wi = {
		DEVELOPMENT_SETUP: "copilot-development-setup-user-guide",
		FEATURES: "copilot-features-panel",
		FEEDBACK: "copilot-feedback-panel",
		INFO: "copilot-info-panel",
		LOG: "copilot-log-panel",
		SETTINGS: "copilot-settings-panel",
		IMPERSONATOR: "copilot-impersonator",
		A11Y_CHECKER: "copilot-a11y-checker",
		BACKEND_AND_DATA: "copilot-backend-and-data-panel",
		COMPONENT_PROPERTIES: "copilot-component-properties",
		CONNECT_TO_SERVICE: "copilot-connect-to-service",
		DOCS: "copilot-docs",
		EDIT_COMPONENT: "copilot-edit-component",
		I18N: "copilot-i18n-panel",
		NEW_ROUTE: "copilot-new-route",
		OUTLINE: "copilot-outline-panel",
		PALETTE: "copilot-palette",
		ROUTES: "copilot-routes-panel",
		SPRING_SECURITY: "copilot-spring-security",
		TEST_BENCH_TEST_GENERATOR: "copilot-test-bench-test-generator-panel",
		THEME_EDITOR: "copilot-theme-editor-panel",
		UI_SERVICES: "copilot-ui-services-panel",
		UI_TEST_GENERATOR: "copilot-ui-test-generator-panel",
		VAADIN_VERSIONS: "copilot-vaadin-versions",
		COMMENTS: "copilot-comments-panel",
		NEW_COMMENT: "copilot-new-comment",
		TEST_FOO_PANEL: "foo-panel",
		TEST_LOG_PANEL: "test-log-panel",
		TEST_PLUGIN_PANEL: "test-plugin-panel"
	}, Ti = new Set([
		wi.DEVELOPMENT_SETUP,
		wi.FEATURES,
		wi.FEEDBACK,
		wi.INFO,
		wi.LOG,
		wi.IMPERSONATOR
	]);
})), Di, Oi, ki, Ai, ji, Mi, Ni, Pi, Fi, Ii, Li, Ri = e((() => {
	Di = Symbol.for("react.portal"), Oi = Symbol.for("react.fragment"), ki = Symbol.for("react.strict_mode"), Ai = Symbol.for("react.profiler"), ji = Symbol.for("react.provider"), Mi = Symbol.for("react.context"), Ni = Symbol.for("react.forward_ref"), Pi = Symbol.for("react.suspense"), Fi = Symbol.for("react.suspense_list"), Ii = Symbol.for("react.memo"), Li = Symbol.for("react.lazy");
}));
//#endregion
//#region frontend/copilot/shared/from-react/getComponentNameFromType.ts
function zi(e, t, n) {
	let r = e.displayName;
	if (r) return r;
	let i = t.displayName || t.name || "";
	return i === "" ? n : `${n}(${i})`;
}
function Bi(e) {
	return e.displayName || "Context";
}
function Vi(e) {
	if (e === null) return null;
	if (typeof e == "function") return e.displayName || e.name || null;
	if (typeof e == "string") return e;
	switch (e) {
		case Oi: return "Fragment";
		case Di: return "Portal";
		case Ai: return "Profiler";
		case ki: return "StrictMode";
		case Pi: return "Suspense";
		case Fi: return "SuspenseList";
	}
	if (typeof e == "object") switch (e.$$typeof) {
		case Mi: return `${Bi(e)}.Consumer`;
		case ji: return `${Bi(e._context)}.Provider`;
		case Ni: return zi(e, e.render, "ForwardRef");
		case Ii:
			let t = e.displayName || null;
			return t === null ? Vi(e.type) || "Memo" : t;
		case Li: {
			let t = e, n = t._payload, r = t._init;
			try {
				return Vi(r(n));
			} catch {
				return null;
			}
		}
	}
	return null;
}
var Hi = e((() => {
	Ri();
}));
//#endregion
//#region frontend/copilot/shared/react-utils.ts
function Ui() {
	let e = /* @__PURE__ */ new Set();
	return Array.from(document.body.querySelectorAll("*")).flatMap(qi).filter(Wi).filter((e) => !e.fileName.includes("frontend/generated/")).forEach((t) => e.add(t.fileName)), Array.from(e);
}
function Wi(e) {
	return !!e && e.fileName;
}
function Gi(e) {
	if (!e) return;
	if (e._debugSource) return e._debugSource;
	let t = e._debugInfo?.source;
	if (t?.fileName && t?.lineNumber) return t;
}
function Ki(e) {
	if (e && e.type?.__debugSourceDefine) return e.type.__debugSourceDefine;
}
function qi(e) {
	return Gi(ra(e));
}
function Ji() {
	return `__reactFiber$${Xi()}`;
}
function Yi() {
	return `__reactContainer$${Xi()}`;
}
function Xi() {
	if (!(!ca && (ca = Array.from(document.querySelectorAll("*")).flatMap((e) => Object.keys(e)).filter((e) => e.startsWith("__reactFiber$")).map((e) => e.replace("__reactFiber$", "")).find((e) => e), !ca))) return ca;
}
function Zi(e) {
	let t = e.type;
	return t?.$$typeof === Ni && !t.displayName && e.child ? Zi(e.child) : Vi(e.type) ?? Vi(e.elementType) ?? "???";
}
function Qi() {
	return na(na(Array.from(document.querySelectorAll("body > *")).flatMap((e) => e[Yi()]).find((e) => e))?.child);
}
function $i(e) {
	let t = [], n = na(e.child);
	for (; n;) t.push(n), n = na(n.sibling);
	return t;
}
function ea(e) {
	return e.hasOwnProperty("entanglements") && e.hasOwnProperty("containerInfo");
}
function ta(e) {
	return e.hasOwnProperty("stateNode") && e.hasOwnProperty("pendingProps");
}
function na(e) {
	let t = e?.stateNode;
	if (t?.current && (ea(t) || ta(t))) return t?.current;
	if (!e) return;
	if (!e.alternate) return e;
	let n = e.alternate, r = e?.actualStartTime, i = n?.actualStartTime;
	return i === r ? e : i > r ? n : e;
}
function ra(e) {
	let t = na(e[Ji()]);
	if (Gi(t)) return t;
	let n = t?.return || void 0;
	for (; n && !Gi(n);) n = n.return || void 0;
	return n;
}
function ia(e) {
	if (e.stateNode?.isConnected === !0) return e.stateNode;
	if (e.child) return ia(e.child);
}
function aa(e) {
	let t = ia(e);
	return t && na(ra(t)) === e;
}
function oa(e) {
	return typeof e.type != "function" || sa(e) ? !1 : !!(Gi(e) || Ki(e));
}
function sa(e) {
	if (!e) return !1;
	let t = e;
	return typeof e.type == "function" && t.tag === 1;
}
var ca, la = e((() => {
	Hi(), Ri(), ca = void 0;
})), ua, da = e((() => {
	ua = async (e, t, n) => window.Vaadin.copilot.comm(e, t, n);
}));
//#endregion
//#region frontend/copilot/shared/flow-utils.ts
function fa(e) {
	return e === void 0 ? !1 : e.nodeId >= 0;
}
function pa(e) {
	if (e.javaClass) return Aa(e.javaClass);
}
function ma(e) {
	let t = window.Vaadin;
	if (t && t.Flow) {
		let { clients: n } = t.Flow, r = Object.keys(n);
		for (let t of r) {
			let r = n[t];
			if (r.getNodeId) {
				let t = r.getNodeId(e);
				if (t >= 0) {
					let n = r.getNodeInfo(t);
					return {
						nodeId: t,
						uiId: r.getUIId(),
						element: e,
						javaClass: n.javaClass,
						styles: n.styles,
						hiddenByServer: n.hiddenByServer
					};
				}
			}
		}
	}
}
function ha() {
	let e = window.Vaadin, t;
	if (e && e.Flow) {
		let { clients: n } = e.Flow, r = Object.keys(n);
		for (let e of r) {
			let r = n[e];
			r.getUIId && (t = r.getUIId());
		}
	}
	return t;
}
function ga(e) {
	return {
		uiId: e.uiId,
		nodeId: e.nodeId
	};
}
function _a(e) {
	return e ? e.type?.type === "FlowContainer" : !1;
}
function va(e) {
	return e.localName.startsWith("flow-container");
}
var ya = e((() => {
	ja();
}));
//#endregion
//#region frontend/copilot/shared/misc-utils.ts
function ba(e, t) {
	let n = e();
	n ? t(n) : setTimeout(() => ba(e, t), 50);
}
async function xa(e) {
	let t = e();
	if (t) return t;
	let n, r = new Promise((e) => {
		n = e;
	}), i = setInterval(() => {
		let t = e();
		t && (clearInterval(i), n(t));
	}, 10);
	return r;
}
function Sa(e) {
	return B.box(e, { deep: !1 });
}
function Ca(e) {
	return e && typeof e.lastAccessedBy_ == "number";
}
function wa(e) {
	if (e) {
		if (typeof e == "string") return e;
		if (!Ca(e)) throw Error(`Expected message to be a string or an observable value but was ${JSON.stringify(e)}`);
		return e.get();
	}
}
function Ta(e) {
	return Array.from(new Set(e));
}
function Ea(e) {
	import("./copilot-notification-CfURs1c4.js").then(({ showNotification: t }) => {
		t(e);
	});
}
function Da() {
	Ea({
		type: Ci.INFORMATION,
		message: "The previous operation is still in progress. Please wait for it to finish."
	});
}
function Oa(e) {
	let t = `--${e}`, n = /* @__PURE__ */ new Set();
	function r(e) {
		return "cssRules" in e;
	}
	function i(e) {
		return e.type === CSSRule.STYLE_RULE;
	}
	function a(e) {
		return "cssRules" in e;
	}
	function o(e) {
		if (!e) return !1;
		for (let n = 0; n < e.length; n++) if (e[n]?.startsWith(t)) return !0;
		return !1;
	}
	function s(e) {
		if (i(e) && o(e.style)) return !0;
		if (a(e)) {
			let t = e.cssRules;
			for (let e of t) if (s(e)) return !0;
		}
		if (e.type === CSSRule.IMPORT_RULE) {
			let t = e;
			if (t.styleSheet && c(t.styleSheet)) return !0;
		}
		return !1;
	}
	function c(e) {
		if (!e || n.has(e)) return !1;
		n.add(e);
		let t;
		try {
			t = e.cssRules;
		} catch {
			return !1;
		}
		if (!t) return !1;
		for (let e of t) if (s(e)) return !0;
		return !1;
	}
	for (let e of Array.from([...document.adoptedStyleSheets, ...document.styleSheets])) if (r(e) && c(e)) return !0;
	return !1;
}
function ka(e) {
	return e?.replace(/^.*[\\/]/, "");
}
function Aa(e) {
	let t = e.lastIndexOf(".");
	return t === -1 ? e : e.substring(t + 1);
}
var ja = e((() => {
	xi(), Ei(), ya();
}));
//#endregion
//#region frontend/copilot/shared/vaadin-dev-tools-integration.ts
async function Ma() {
	return xa(() => {
		let e = window.Vaadin.devTools, t = e?.frontendConnection && e?.frontendConnection.status === "active";
		return e !== void 0 && t && e?.frontendConnection;
	});
}
function Na(e, t) {
	Ma().then((n) => {
		n.canSend ? n.send(e, t) : Ea({
			type: Ci.INFORMATION,
			message: "Connection lost",
			details: "Please refresh the page and start the server if it is not running",
			delay: 1e4,
			dismissId: "connection-lost"
		});
	});
}
var Pa = e((() => {
	ja(), Ei();
})), Fa, Ia, La, Ra, za, Ba, Va, Ha, Ua, Wa = e((() => {
	Fa = globalThis, Ia = Fa.ShadowRoot && (Fa.ShadyCSS === void 0 || Fa.ShadyCSS.nativeShadow) && "adoptedStyleSheets" in Document.prototype && "replace" in CSSStyleSheet.prototype, La = Symbol(), Ra = /* @__PURE__ */ new WeakMap(), za = class {
		constructor(e, t, n) {
			if (this._$cssResult$ = !0, n !== La) throw Error("CSSResult is not constructable. Use `unsafeCSS` or `css` instead.");
			this.cssText = e, this.t = t;
		}
		get styleSheet() {
			let e = this.o, t = this.t;
			if (Ia && e === void 0) {
				let n = t !== void 0 && t.length === 1;
				n && (e = Ra.get(t)), e === void 0 && ((this.o = e = new CSSStyleSheet()).replaceSync(this.cssText), n && Ra.set(t, e));
			}
			return e;
		}
		toString() {
			return this.cssText;
		}
	}, Ba = (e) => new za(typeof e == "string" ? e : e + "", void 0, La), Va = (e, ...t) => new za(e.length === 1 ? e[0] : t.reduce(((t, n, r) => t + ((e) => {
		if (!0 === e._$cssResult$) return e.cssText;
		if (typeof e == "number") return e;
		throw Error("Value passed to 'css' function must be a 'css' function result: " + e + ". Use 'unsafeCSS' to pass non-literal values, but take care to ensure page security.");
	})(n) + e[r + 1]), e[0]), e, La), Ha = (e, t) => {
		if (Ia) e.adoptedStyleSheets = t.map(((e) => e instanceof CSSStyleSheet ? e : e.styleSheet));
		else for (let n of t) {
			let t = document.createElement("style"), r = Fa.litNonce;
			r !== void 0 && t.setAttribute("nonce", r), t.textContent = n.cssText, e.appendChild(t);
		}
	}, Ua = Ia ? (e) => e : (e) => e instanceof CSSStyleSheet ? ((e) => {
		let t = "";
		for (let n of e.cssRules) t += n.cssText;
		return Ba(t);
	})(e) : e;
})), Ga, Ka, qa, Ja, Ya, Xa, Za, Qa, $a, eo, to, no, ro, io, ao, oo = e((() => {
	Wa(), {is: Ga, defineProperty: Ka, getOwnPropertyDescriptor: qa, getOwnPropertyNames: Ja, getOwnPropertySymbols: Ya, getPrototypeOf: Xa} = Object, Za = globalThis, Qa = Za.trustedTypes, $a = Qa ? Qa.emptyScript : "", eo = Za.reactiveElementPolyfillSupport, to = (e, t) => e, no = {
		toAttribute(e, t) {
			switch (t) {
				case Boolean:
					e = e ? $a : null;
					break;
				case Object:
				case Array: e = e == null ? e : JSON.stringify(e);
			}
			return e;
		},
		fromAttribute(e, t) {
			let n = e;
			switch (t) {
				case Boolean:
					n = e !== null;
					break;
				case Number:
					n = e === null ? null : Number(e);
					break;
				case Object:
				case Array: try {
					n = JSON.parse(e);
				} catch {
					n = null;
				}
			}
			return n;
		}
	}, ro = (e, t) => !Ga(e, t), io = {
		attribute: !0,
		type: String,
		converter: no,
		reflect: !1,
		useDefault: !1,
		hasChanged: ro
	}, Symbol.metadata ??= Symbol("metadata"), Za.litPropertyMetadata ??= /* @__PURE__ */ new WeakMap(), ao = class extends HTMLElement {
		static addInitializer(e) {
			this._$Ei(), (this.l ??= []).push(e);
		}
		static get observedAttributes() {
			return this.finalize(), this._$Eh && [...this._$Eh.keys()];
		}
		static createProperty(e, t = io) {
			if (t.state && (t.attribute = !1), this._$Ei(), this.prototype.hasOwnProperty(e) && ((t = Object.create(t)).wrapped = !0), this.elementProperties.set(e, t), !t.noAccessor) {
				let n = Symbol(), r = this.getPropertyDescriptor(e, n, t);
				r !== void 0 && Ka(this.prototype, e, r);
			}
		}
		static getPropertyDescriptor(e, t, n) {
			let { get: r, set: i } = qa(this.prototype, e) ?? {
				get() {
					return this[t];
				},
				set(e) {
					this[t] = e;
				}
			};
			return {
				get: r,
				set(t) {
					let a = r?.call(this);
					i?.call(this, t), this.requestUpdate(e, a, n);
				},
				configurable: !0,
				enumerable: !0
			};
		}
		static getPropertyOptions(e) {
			return this.elementProperties.get(e) ?? io;
		}
		static _$Ei() {
			if (this.hasOwnProperty(to("elementProperties"))) return;
			let e = Xa(this);
			e.finalize(), e.l !== void 0 && (this.l = [...e.l]), this.elementProperties = new Map(e.elementProperties);
		}
		static finalize() {
			if (this.hasOwnProperty(to("finalized"))) return;
			if (this.finalized = !0, this._$Ei(), this.hasOwnProperty(to("properties"))) {
				let e = this.properties, t = [...Ja(e), ...Ya(e)];
				for (let n of t) this.createProperty(n, e[n]);
			}
			let e = this[Symbol.metadata];
			if (e !== null) {
				let t = litPropertyMetadata.get(e);
				if (t !== void 0) for (let [e, n] of t) this.elementProperties.set(e, n);
			}
			this._$Eh = /* @__PURE__ */ new Map();
			for (let [e, t] of this.elementProperties) {
				let n = this._$Eu(e, t);
				n !== void 0 && this._$Eh.set(n, e);
			}
			this.elementStyles = this.finalizeStyles(this.styles);
		}
		static finalizeStyles(e) {
			let t = [];
			if (Array.isArray(e)) {
				let n = new Set(e.flat(Infinity).reverse());
				for (let e of n) t.unshift(Ua(e));
			} else e !== void 0 && t.push(Ua(e));
			return t;
		}
		static _$Eu(e, t) {
			let n = t.attribute;
			return !1 === n ? void 0 : typeof n == "string" ? n : typeof e == "string" ? e.toLowerCase() : void 0;
		}
		constructor() {
			super(), this._$Ep = void 0, this.isUpdatePending = !1, this.hasUpdated = !1, this._$Em = null, this._$Ev();
		}
		_$Ev() {
			this._$ES = new Promise(((e) => this.enableUpdating = e)), this._$AL = /* @__PURE__ */ new Map(), this._$E_(), this.requestUpdate(), this.constructor.l?.forEach(((e) => e(this)));
		}
		addController(e) {
			(this._$EO ??= /* @__PURE__ */ new Set()).add(e), this.renderRoot !== void 0 && this.isConnected && e.hostConnected?.();
		}
		removeController(e) {
			this._$EO?.delete(e);
		}
		_$E_() {
			let e = /* @__PURE__ */ new Map(), t = this.constructor.elementProperties;
			for (let n of t.keys()) this.hasOwnProperty(n) && (e.set(n, this[n]), delete this[n]);
			e.size > 0 && (this._$Ep = e);
		}
		createRenderRoot() {
			let e = this.shadowRoot ?? this.attachShadow(this.constructor.shadowRootOptions);
			return Ha(e, this.constructor.elementStyles), e;
		}
		connectedCallback() {
			this.renderRoot ??= this.createRenderRoot(), this.enableUpdating(!0), this._$EO?.forEach(((e) => e.hostConnected?.()));
		}
		enableUpdating(e) {}
		disconnectedCallback() {
			this._$EO?.forEach(((e) => e.hostDisconnected?.()));
		}
		attributeChangedCallback(e, t, n) {
			this._$AK(e, n);
		}
		_$ET(e, t) {
			let n = this.constructor.elementProperties.get(e), r = this.constructor._$Eu(e, n);
			if (r !== void 0 && !0 === n.reflect) {
				let i = (n.converter?.toAttribute === void 0 ? no : n.converter).toAttribute(t, n.type);
				this._$Em = e, i == null ? this.removeAttribute(r) : this.setAttribute(r, i), this._$Em = null;
			}
		}
		_$AK(e, t) {
			let n = this.constructor, r = n._$Eh.get(e);
			if (r !== void 0 && this._$Em !== r) {
				let e = n.getPropertyOptions(r), i = typeof e.converter == "function" ? { fromAttribute: e.converter } : e.converter?.fromAttribute === void 0 ? no : e.converter;
				this._$Em = r, this[r] = i.fromAttribute(t, e.type) ?? this._$Ej?.get(r) ?? null, this._$Em = null;
			}
		}
		requestUpdate(e, t, n) {
			if (e !== void 0) {
				let r = this.constructor, i = this[e];
				if (n ??= r.getPropertyOptions(e), !((n.hasChanged ?? ro)(i, t) || n.useDefault && n.reflect && i === this._$Ej?.get(e) && !this.hasAttribute(r._$Eu(e, n)))) return;
				this.C(e, t, n);
			}
			!1 === this.isUpdatePending && (this._$ES = this._$EP());
		}
		C(e, t, { useDefault: n, reflect: r, wrapped: i }, a) {
			n && !(this._$Ej ??= /* @__PURE__ */ new Map()).has(e) && (this._$Ej.set(e, a ?? t ?? this[e]), !0 !== i || a !== void 0) || (this._$AL.has(e) || (this.hasUpdated || n || (t = void 0), this._$AL.set(e, t)), !0 === r && this._$Em !== e && (this._$Eq ??= /* @__PURE__ */ new Set()).add(e));
		}
		async _$EP() {
			this.isUpdatePending = !0;
			try {
				await this._$ES;
			} catch (e) {
				Promise.reject(e);
			}
			let e = this.scheduleUpdate();
			return e != null && await e, !this.isUpdatePending;
		}
		scheduleUpdate() {
			return this.performUpdate();
		}
		performUpdate() {
			if (!this.isUpdatePending) return;
			if (!this.hasUpdated) {
				if (this.renderRoot ??= this.createRenderRoot(), this._$Ep) {
					for (let [e, t] of this._$Ep) this[e] = t;
					this._$Ep = void 0;
				}
				let e = this.constructor.elementProperties;
				if (e.size > 0) for (let [t, n] of e) {
					let { wrapped: e } = n, r = this[t];
					!0 !== e || this._$AL.has(t) || r === void 0 || this.C(t, void 0, n, r);
				}
			}
			let e = !1, t = this._$AL;
			try {
				e = this.shouldUpdate(t), e ? (this.willUpdate(t), this._$EO?.forEach(((e) => e.hostUpdate?.())), this.update(t)) : this._$EM();
			} catch (t) {
				throw e = !1, this._$EM(), t;
			}
			e && this._$AE(t);
		}
		willUpdate(e) {}
		_$AE(e) {
			this._$EO?.forEach(((e) => e.hostUpdated?.())), this.hasUpdated || (this.hasUpdated = !0, this.firstUpdated(e)), this.updated(e);
		}
		_$EM() {
			this._$AL = /* @__PURE__ */ new Map(), this.isUpdatePending = !1;
		}
		get updateComplete() {
			return this.getUpdateComplete();
		}
		getUpdateComplete() {
			return this._$ES;
		}
		shouldUpdate(e) {
			return !0;
		}
		update(e) {
			this._$Eq &&= this._$Eq.forEach(((e) => this._$ET(e, this[e]))), this._$EM();
		}
		updated(e) {}
		firstUpdated(e) {}
	}, ao.elementStyles = [], ao.shadowRootOptions = { mode: "open" }, ao[to("elementProperties")] = /* @__PURE__ */ new Map(), ao[to("finalized")] = /* @__PURE__ */ new Map(), eo?.({ ReactiveElement: ao }), (Za.reactiveElementVersions ??= []).push("2.1.0");
}));
//#endregion
//#region node_modules/lit-html/lit-html.js
function so(e, t) {
	if (!yo(e) || !e.hasOwnProperty("raw")) throw Error("invalid template strings array");
	return fo === void 0 ? t : fo.createHTML(t);
}
function co(e, t, n = e, r) {
	if (t === jo) return t;
	let i = r === void 0 ? n._$Cl : n._$Co?.[r], a = vo(t) ? void 0 : t._$litDirective$;
	return i?.constructor !== a && (i?._$AO?.(!1), a === void 0 ? i = void 0 : (i = new a(e), i._$AT(e, n, r)), r === void 0 ? n._$Cl = i : (n._$Co ??= [])[r] = i), i !== void 0 && (t = co(e, i._$AS(e, t.values), i, r)), t;
}
var lo, uo, fo, po, Z, mo, ho, go, _o, vo, yo, bo, xo, So, Co, wo, To, Eo, Do, Oo, ko, Ao, Q, jo, $, Mo, No, Po, Fo, Io, Lo, Ro, zo, Bo, Vo, Ho, Uo, Wo, Go, Ko = e((() => {
	lo = globalThis, uo = lo.trustedTypes, fo = uo ? uo.createPolicy("lit-html", { createHTML: (e) => e }) : void 0, po = "$lit$", Z = `lit$${Math.random().toFixed(9).slice(2)}$`, mo = "?" + Z, ho = `<${mo}>`, go = document, _o = () => go.createComment(""), vo = (e) => e === null || typeof e != "object" && typeof e != "function", yo = Array.isArray, bo = (e) => yo(e) || typeof e?.[Symbol.iterator] == "function", xo = "[ 	\n\f\r]", So = /<(?:(!--|\/[^a-zA-Z])|(\/?[a-zA-Z][^>\s]*)|(\/?$))/g, Co = /-->/g, wo = />/g, To = RegExp(`>|${xo}(?:([^\\s"'>=/]+)(${xo}*=${xo}*(?:[^ \t\n\f\r"'\`<>=]|("|')|))|$)`, "g"), Eo = /'/g, Do = /"/g, Oo = /^(?:script|style|textarea|title)$/i, ko = (e) => (t, ...n) => ({
		_$litType$: e,
		strings: t,
		values: n
	}), Ao = ko(1), Q = ko(2), ko(3), jo = Symbol.for("lit-noChange"), $ = Symbol.for("lit-nothing"), Mo = /* @__PURE__ */ new WeakMap(), No = go.createTreeWalker(go, 129), Po = (e, t) => {
		let n = e.length - 1, r = [], i, a = t === 2 ? "<svg>" : t === 3 ? "<math>" : "", o = So;
		for (let t = 0; t < n; t++) {
			let n = e[t], s, c, l = -1, u = 0;
			for (; u < n.length && (o.lastIndex = u, c = o.exec(n), c !== null);) u = o.lastIndex, o === So ? c[1] === "!--" ? o = Co : c[1] === void 0 ? c[2] === void 0 ? c[3] !== void 0 && (o = To) : (Oo.test(c[2]) && (i = RegExp("</" + c[2], "g")), o = To) : o = wo : o === To ? c[0] === ">" ? (o = i ?? So, l = -1) : c[1] === void 0 ? l = -2 : (l = o.lastIndex - c[2].length, s = c[1], o = c[3] === void 0 ? To : c[3] === "\"" ? Do : Eo) : o === Do || o === Eo ? o = To : o === Co || o === wo ? o = So : (o = To, i = void 0);
			let d = o === To && e[t + 1].startsWith("/>") ? " " : "";
			a += o === So ? n + ho : l >= 0 ? (r.push(s), n.slice(0, l) + po + n.slice(l) + Z + d) : n + Z + (l === -2 ? t : d);
		}
		return [so(e, a + (e[n] || "<?>") + (t === 2 ? "</svg>" : t === 3 ? "</math>" : "")), r];
	}, Fo = class e {
		constructor({ strings: t, _$litType$: n }, r) {
			let i;
			this.parts = [];
			let a = 0, o = 0, s = t.length - 1, c = this.parts, [l, u] = Po(t, n);
			if (this.el = e.createElement(l, r), No.currentNode = this.el.content, n === 2 || n === 3) {
				let e = this.el.content.firstChild;
				e.replaceWith(...e.childNodes);
			}
			for (; (i = No.nextNode()) !== null && c.length < s;) {
				if (i.nodeType === 1) {
					if (i.hasAttributes()) for (let e of i.getAttributeNames()) if (e.endsWith(po)) {
						let t = u[o++], n = i.getAttribute(e).split(Z), r = /([.?@])?(.*)/.exec(t);
						c.push({
							type: 1,
							index: a,
							name: r[2],
							strings: n,
							ctor: r[1] === "." ? zo : r[1] === "?" ? Bo : r[1] === "@" ? Vo : Ro
						}), i.removeAttribute(e);
					} else e.startsWith(Z) && (c.push({
						type: 6,
						index: a
					}), i.removeAttribute(e));
					if (Oo.test(i.tagName)) {
						let e = i.textContent.split(Z), t = e.length - 1;
						if (t > 0) {
							i.textContent = uo ? uo.emptyScript : "";
							for (let n = 0; n < t; n++) i.append(e[n], _o()), No.nextNode(), c.push({
								type: 2,
								index: ++a
							});
							i.append(e[t], _o());
						}
					}
				} else if (i.nodeType === 8) if (i.data === mo) c.push({
					type: 2,
					index: a
				});
				else {
					let e = -1;
					for (; (e = i.data.indexOf(Z, e + 1)) !== -1;) c.push({
						type: 7,
						index: a
					}), e += Z.length - 1;
				}
				a++;
			}
		}
		static createElement(e, t) {
			let n = go.createElement("template");
			return n.innerHTML = e, n;
		}
	}, Io = class {
		constructor(e, t) {
			this._$AV = [], this._$AN = void 0, this._$AD = e, this._$AM = t;
		}
		get parentNode() {
			return this._$AM.parentNode;
		}
		get _$AU() {
			return this._$AM._$AU;
		}
		u(e) {
			let { el: { content: t }, parts: n } = this._$AD, r = (e?.creationScope ?? go).importNode(t, !0);
			No.currentNode = r;
			let i = No.nextNode(), a = 0, o = 0, s = n[0];
			for (; s !== void 0;) {
				if (a === s.index) {
					let t;
					s.type === 2 ? t = new Lo(i, i.nextSibling, this, e) : s.type === 1 ? t = new s.ctor(i, s.name, s.strings, this, e) : s.type === 6 && (t = new Ho(i, this, e)), this._$AV.push(t), s = n[++o];
				}
				a !== s?.index && (i = No.nextNode(), a++);
			}
			return No.currentNode = go, r;
		}
		p(e) {
			let t = 0;
			for (let n of this._$AV) n !== void 0 && (n.strings === void 0 ? n._$AI(e[t]) : (n._$AI(e, n, t), t += n.strings.length - 2)), t++;
		}
	}, Lo = class e {
		get _$AU() {
			return this._$AM?._$AU ?? this._$Cv;
		}
		constructor(e, t, n, r) {
			this.type = 2, this._$AH = $, this._$AN = void 0, this._$AA = e, this._$AB = t, this._$AM = n, this.options = r, this._$Cv = r?.isConnected ?? !0;
		}
		get parentNode() {
			let e = this._$AA.parentNode, t = this._$AM;
			return t !== void 0 && e?.nodeType === 11 && (e = t.parentNode), e;
		}
		get startNode() {
			return this._$AA;
		}
		get endNode() {
			return this._$AB;
		}
		_$AI(e, t = this) {
			e = co(this, e, t), vo(e) ? e === $ || e == null || e === "" ? (this._$AH !== $ && this._$AR(), this._$AH = $) : e !== this._$AH && e !== jo && this._(e) : e._$litType$ === void 0 ? e.nodeType === void 0 ? bo(e) ? this.k(e) : this._(e) : this.T(e) : this.$(e);
		}
		O(e) {
			return this._$AA.parentNode.insertBefore(e, this._$AB);
		}
		T(e) {
			this._$AH !== e && (this._$AR(), this._$AH = this.O(e));
		}
		_(e) {
			this._$AH !== $ && vo(this._$AH) ? this._$AA.nextSibling.data = e : this.T(go.createTextNode(e)), this._$AH = e;
		}
		$(e) {
			let { values: t, _$litType$: n } = e, r = typeof n == "number" ? this._$AC(e) : (n.el === void 0 && (n.el = Fo.createElement(so(n.h, n.h[0]), this.options)), n);
			if (this._$AH?._$AD === r) this._$AH.p(t);
			else {
				let e = new Io(r, this), n = e.u(this.options);
				e.p(t), this.T(n), this._$AH = e;
			}
		}
		_$AC(e) {
			let t = Mo.get(e.strings);
			return t === void 0 && Mo.set(e.strings, t = new Fo(e)), t;
		}
		k(t) {
			yo(this._$AH) || (this._$AH = [], this._$AR());
			let n = this._$AH, r, i = 0;
			for (let a of t) i === n.length ? n.push(r = new e(this.O(_o()), this.O(_o()), this, this.options)) : r = n[i], r._$AI(a), i++;
			i < n.length && (this._$AR(r && r._$AB.nextSibling, i), n.length = i);
		}
		_$AR(e = this._$AA.nextSibling, t) {
			for (this._$AP?.(!1, !0, t); e && e !== this._$AB;) {
				let t = e.nextSibling;
				e.remove(), e = t;
			}
		}
		setConnected(e) {
			this._$AM === void 0 && (this._$Cv = e, this._$AP?.(e));
		}
	}, Ro = class {
		get tagName() {
			return this.element.tagName;
		}
		get _$AU() {
			return this._$AM._$AU;
		}
		constructor(e, t, n, r, i) {
			this.type = 1, this._$AH = $, this._$AN = void 0, this.element = e, this.name = t, this._$AM = r, this.options = i, n.length > 2 || n[0] !== "" || n[1] !== "" ? (this._$AH = Array(n.length - 1).fill(/* @__PURE__ */ new String()), this.strings = n) : this._$AH = $;
		}
		_$AI(e, t = this, n, r) {
			let i = this.strings, a = !1;
			if (i === void 0) e = co(this, e, t, 0), a = !vo(e) || e !== this._$AH && e !== jo, a && (this._$AH = e);
			else {
				let r = e, o, s;
				for (e = i[0], o = 0; o < i.length - 1; o++) s = co(this, r[n + o], t, o), s === jo && (s = this._$AH[o]), a ||= !vo(s) || s !== this._$AH[o], s === $ ? e = $ : e !== $ && (e += (s ?? "") + i[o + 1]), this._$AH[o] = s;
			}
			a && !r && this.j(e);
		}
		j(e) {
			e === $ ? this.element.removeAttribute(this.name) : this.element.setAttribute(this.name, e ?? "");
		}
	}, zo = class extends Ro {
		constructor() {
			super(...arguments), this.type = 3;
		}
		j(e) {
			this.element[this.name] = e === $ ? void 0 : e;
		}
	}, Bo = class extends Ro {
		constructor() {
			super(...arguments), this.type = 4;
		}
		j(e) {
			this.element.toggleAttribute(this.name, !!e && e !== $);
		}
	}, Vo = class extends Ro {
		constructor(e, t, n, r, i) {
			super(e, t, n, r, i), this.type = 5;
		}
		_$AI(e, t = this) {
			if ((e = co(this, e, t, 0) ?? $) === jo) return;
			let n = this._$AH, r = e === $ && n !== $ || e.capture !== n.capture || e.once !== n.once || e.passive !== n.passive, i = e !== $ && (n === $ || r);
			r && this.element.removeEventListener(this.name, this, n), i && this.element.addEventListener(this.name, this, e), this._$AH = e;
		}
		handleEvent(e) {
			typeof this._$AH == "function" ? this._$AH.call(this.options?.host ?? this.element, e) : this._$AH.handleEvent(e);
		}
	}, Ho = class {
		constructor(e, t, n) {
			this.element = e, this.type = 6, this._$AN = void 0, this._$AM = t, this.options = n;
		}
		get _$AU() {
			return this._$AM._$AU;
		}
		_$AI(e) {
			co(this, e);
		}
	}, Uo = {
		M: po,
		P: Z,
		A: mo,
		C: 1,
		L: Po,
		R: Io,
		D: bo,
		V: co,
		I: Lo,
		H: Ro,
		N: Bo,
		U: Vo,
		B: zo,
		F: Ho
	}, Wo = lo.litHtmlPolyfillSupport, Wo?.(Fo, Lo), (lo.litHtmlVersions ??= []).push("3.3.0"), Go = (e, t, n) => {
		let r = n?.renderBefore ?? t, i = r._$litPart$;
		if (i === void 0) {
			let e = n?.renderBefore ?? null;
			r._$litPart$ = i = new Lo(t.insertBefore(_o(), e), e, void 0, n ?? {});
		}
		return i._$AI(e), i;
	};
})), qo, Jo, Yo, Xo = e((() => {
	oo(), oo(), Ko(), Ko(), qo = globalThis, Jo = class extends ao {
		constructor() {
			super(...arguments), this.renderOptions = { host: this }, this._$Do = void 0;
		}
		createRenderRoot() {
			let e = super.createRenderRoot();
			return this.renderOptions.renderBefore ??= e.firstChild, e;
		}
		update(e) {
			let t = this.render();
			this.hasUpdated || (this.renderOptions.isConnected = this.isConnected), super.update(e), this._$Do = Go(t, this.renderRoot, this.renderOptions);
		}
		connectedCallback() {
			super.connectedCallback(), this._$Do?.setConnected(!0);
		}
		disconnectedCallback() {
			super.disconnectedCallback(), this._$Do?.setConnected(!1);
		}
		render() {
			return jo;
		}
	}, Jo._$litElement$ = !0, Jo.finalized = !0, qo.litElementHydrateSupport?.({ LitElement: Jo }), Yo = qo.litElementPolyfillSupport, Yo?.({ LitElement: Jo }), (qo.litElementVersions ??= []).push("4.2.0");
})), Zo = e((() => {})), Qo = e((() => {
	oo(), Ko(), Xo(), Zo();
})), $o, es = e((() => {
	Qo(), $o = {
		_123: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M220-360v-180h-60v-60h120v240h-60Zm140 0v-100q0-17 11.5-28.5T400-500h80v-40H360v-60h140q17 0 28.5 11.5T540-560v60q0 17-11.5 28.5T500-460h-80v40h120v60H360Zm240 0v-60h120v-40h-80v-40h80v-40H600v-60h140q17 0 28.5 11.5T780-560v160q0 17-11.5 28.5T740-360H600Z"/></svg>`,
		accessibilityNew: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-720q-33 0-56.5-23.5T400-800q0-33 23.5-56.5T480-880q33 0 56.5 23.5T560-800q0 33-23.5 56.5T480-720ZM360-80v-520q-60-5-122-15t-118-25l20-80q78 21 166 30.5t174 9.5q86 0 174-9.5T820-720l20 80q-56 15-118 25t-122 15v520h-80v-240h-80v240h-80Z"/></svg>`,
		accountCircle: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M234-276q51-39 114-61.5T480-360q69 0 132 22.5T726-276q35-41 54.5-93T800-480q0-133-93.5-226.5T480-800q-133 0-226.5 93.5T160-480q0 59 19.5 111t54.5 93Zm246-164q-59 0-99.5-40.5T340-580q0-59 40.5-99.5T480-720q59 0 99.5 40.5T620-580q0 59-40.5 99.5T480-440Zm0 360q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q53 0 100-15.5t86-44.5q-39-29-86-44.5T480-280q-53 0-100 15.5T294-220q39 29 86 44.5T480-160Zm0-360q26 0 43-17t17-43q0-26-17-43t-43-17q-26 0-43 17t-17 43q0 26 17 43t43 17Zm0-60Zm0 360Z"/></svg>`,
		accountTree: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M600-120v-120H440v-400h-80v120H80v-320h280v120h240v-120h280v320H600v-120h-80v320h80v-120h280v320H600ZM160-760v160-160Zm520 400v160-160Zm0-400v160-160Zm0 160h120v-160H680v160Zm0 400h120v-160H680v160ZM160-600h120v-160H160v160Z"/></svg>`,
		add: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M440-440H200v-80h240v-240h80v240h240v80H520v240h-80v-240Z"/></svg>`,
		addHome: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M700-200h40v-100h100v-40H740v-100h-40v100H600v40h100v100Zm20 80q-83 0-141.5-58.5T520-320q0-83 58.5-141.5T720-520q83 0 141.5 58.5T920-320q0 83-58.5 141.5T720-120Zm-560-80v-480l320-240 320 240v92q-19-6-39-9t-41-3v-40L480-820 240-640v360h203q3 21 9 41t15 39H160Zm320-350Z"/></svg>`,
		addLocation: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M440-400h80v-120h120v-80H520v-120h-80v120H320v80h120v120Zm40 214q122-112 181-203.5T720-552q0-109-69.5-178.5T480-800q-101 0-170.5 69.5T240-552q0 71 59 162.5T480-186Zm0 106Q319-217 239.5-334.5T160-552q0-150 96.5-239T480-880q127 0 223.5 89T800-552q0 100-79.5 217.5T480-80Zm0-480Z"/></svg>`,
		addLocationAlt: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-80Q319-217 239.5-334.5T160-552q0-150 96.5-239T480-880h20q10 0 20 2v81q-10-2-19.5-2.5T480-800q-101 0-170.5 69.5T240-552q0 71 59 162.5T480-186q122-112 181-203.5T720-552v-8h80v8q0 100-79.5 217.5T480-80Zm0-400q33 0 56.5-23.5T560-560q0-33-23.5-56.5T480-640q-33 0-56.5 23.5T400-560q0 33 23.5 56.5T480-480Zm0-80Zm240-80h80v-120h120v-80H800v-120h-80v120H600v80h120v120Z"/></svg>`,
		adminPanelSettings: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M680-280q25 0 42.5-17.5T740-340q0-25-17.5-42.5T680-400q-25 0-42.5 17.5T620-340q0 25 17.5 42.5T680-280Zm0 120q31 0 57-14.5t42-38.5q-22-13-47-20t-52-7q-27 0-52 7t-47 20q16 24 42 38.5t57 14.5ZM480-80q-139-35-229.5-159.5T160-516v-244l320-120 320 120v227q-19-8-39-14.5t-41-9.5v-147l-240-90-240 90v188q0 47 12.5 94t35 89.5Q310-290 342-254t71 60q11 32 29 61t41 52q-1 0-1.5.5t-1.5.5Zm200 0q-83 0-141.5-58.5T480-280q0-83 58.5-141.5T680-480q83 0 141.5 58.5T880-280q0 83-58.5 141.5T680-80ZM480-494Z"/></svg>`,
		adsClick: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M468-240q-96-5-162-74t-66-166q0-100 70-170t170-70q97 0 166 66t74 162l-84-25q-13-54-56-88.5T480-640q-66 0-113 47t-47 113q0 57 34.5 100t88.5 56l25 84Zm48 158q-9 2-18 2h-18q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480v18q0 9-2 18l-78-24v-12q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93h12l24 78Zm305 22L650-231 600-80 480-480l400 120-151 50 171 171-79 79Z"/></svg>`,
		adsGroup: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M320-320h480v-400H320v400Zm0 80q-33 0-56.5-23.5T240-320v-480q0-33 23.5-56.5T320-880h480q33 0 56.5 23.5T880-800v480q0 33-23.5 56.5T800-240H320ZM160-80q-33 0-56.5-23.5T80-160v-560h80v560h560v80H160Zm160-720v480-480Z"/></svg>`,
		alignCenter: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M80-440v-80h800v80H80Zm200-120v-120h400v120H280Zm0 280v-120h400v120H280Z"/></svg>`,
		alignEnd: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M80-80v-80h800v80H80Zm200-440v-120h400v120H280Zm0 240v-120h400v120H280Z"/></svg>`,
		alignItemsStretch: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M280-240v-480h120v480H280Zm280 0v-480h120v480H560ZM80-800v-80h800v80H80Zm0 720v-80h800v80H80Z"/></svg>`,
		alignJustifyCenter: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M440-80v-800h80v800h-80Zm160-200v-400h120v400H600Zm-360 0v-400h120v400H240Z"/></svg>`,
		alignJustifyFlexEnd: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M800-80v-800h80v800h-80ZM560-280v-400h120v400H560Zm-240 0v-400h120v400H320Z"/></svg>`,
		alignJustifyFlexStart: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M80-80v-800h80v800H80Zm440-200v-400h120v400H520Zm-240 0v-400h120v400H280Z"/></svg>`,
		alignJustifySpaceAround: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M800-80v-800h80v800h-80ZM80-80v-800h80v800H80Zm520-200v-400h120v400H600Zm-360 0v-400h120v400H240Z"/></svg>`,
		alignJustifySpaceBetween: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M800-80v-200H680v-400h120v-200h80v800h-80ZM80-80v-800h80v200h120v400H160v200H80Z"/></svg>`,
		alignJustifySpaceEven: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M800-80v-800h80v800h-80ZM80-80v-800h80v800H80Zm480-200v-400h120v400H560Zm-280 0v-400h120v400H280Z"/></svg>`,
		alignJustifyStretch: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M800-80v-800h80v800h-80ZM80-80v-800h80v800H80Zm440-480v-120h200v120H520Zm-280 0v-120h200v120H240Zm280 280v-120h200v120H520Zm-280 0v-120h200v120H240Z"/></svg>`,
		alignSelfStretch: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M80-800v-80h800v80H80Zm0 720v-80h800v80H80Zm340-180v-460h120v460H420Z"/></svg>`,
		alignStart: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M80-800v-80h800v80H80Zm200 240v-120h400v120H280Zm0 240v-120h400v120H280Z"/></svg>`,
		alignStretch: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M280-560v-240H80v-80h800v80H680v240H280ZM80-80v-80h200v-240h400v240h200v80H80Z"/></svg>`,
		alternateEmail: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480v58q0 59-40.5 100.5T740-280q-35 0-66-15t-52-43q-29 29-65.5 43.5T480-280q-83 0-141.5-58.5T280-480q0-83 58.5-141.5T480-680q83 0 141.5 58.5T680-480v58q0 26 17 44t43 18q26 0 43-18t17-44v-58q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93h200v80H480Zm0-280q50 0 85-35t35-85q0-50-35-85t-85-35q-50 0-85 35t-35 85q0 50 35 85t85 35Z"/></svg>`,
		appBadging: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-480Zm-400 0q0-88 34-163t93-130q59-55 136-83.5T508-879q17 2 27 14.5t7 29.5q-3 17-16.5 27t-30.5 9q-69-3-129.5 19.5T259-713q-46 44-72.5 103.5T160-480q0 134 93 227t227 93q69 0 128.5-26.5T712-259q46-48 68-109t19-127q-1-17 9-30.5t27-16.5q17-3 29.5 7t14.5 27q6 87-22.5 164T774-208q-57 62-133 95T480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480Zm640-120q-50 0-85-35t-35-85q0-50 35-85t85-35q50 0 85 35t35 85q0 50-35 85t-85 35Z"/></svg>`,
		apple: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 814 1000"><path d="M788.1 340.9c-5.8 4.5-108.2 62.2-108.2 190.5 0 148.4 130.3 200.9 134.2 202.2-.6 3.2-20.7 71.9-68.7 141.9-42.8 61.6-87.5 123.1-155.5 123.1s-85.5-39.5-164-39.5c-76.5 0-103.7 40.8-165.9 40.8s-105.6-57-155.5-127C46.7 790.7 0 663 0 541.8c0-194.4 126.4-297.5 250.8-297.5 66.1 0 121.2 43.4 162.7 43.4 39.5 0 101.1-46 176.3-46 28.5 0 130.9 2.6 198.3 99.2zm-234-181.5c31.1-36.9 53.1-88.1 53.1-139.3 0-7.1-.6-14.3-1.9-20.1-50.6 1.9-110.8 33.7-147.1 75.8-28.5 32.4-55.1 83.6-55.1 135.5 0 7.8 1.3 15.6 1.9 18.1 3.2.6 8.4 1.3 13.6 1.3 45.4 0 102.5-30.4 135.5-71.3z"/></svg>`,
		apps: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M240-160q-33 0-56.5-23.5T160-240q0-33 23.5-56.5T240-320q33 0 56.5 23.5T320-240q0 33-23.5 56.5T240-160Zm240 0q-33 0-56.5-23.5T400-240q0-33 23.5-56.5T480-320q33 0 56.5 23.5T560-240q0 33-23.5 56.5T480-160Zm240 0q-33 0-56.5-23.5T640-240q0-33 23.5-56.5T720-320q33 0 56.5 23.5T800-240q0 33-23.5 56.5T720-160ZM240-400q-33 0-56.5-23.5T160-480q0-33 23.5-56.5T240-560q33 0 56.5 23.5T320-480q0 33-23.5 56.5T240-400Zm240 0q-33 0-56.5-23.5T400-480q0-33 23.5-56.5T480-560q33 0 56.5 23.5T560-480q0 33-23.5 56.5T480-400Zm240 0q-33 0-56.5-23.5T640-480q0-33 23.5-56.5T720-560q33 0 56.5 23.5T800-480q0 33-23.5 56.5T720-400ZM240-640q-33 0-56.5-23.5T160-720q0-33 23.5-56.5T240-800q33 0 56.5 23.5T320-720q0 33-23.5 56.5T240-640Zm240 0q-33 0-56.5-23.5T400-720q0-33 23.5-56.5T480-800q33 0 56.5 23.5T560-720q0 33-23.5 56.5T480-640Zm240 0q-33 0-56.5-23.5T640-720q0-33 23.5-56.5T720-800q33 0 56.5 23.5T800-720q0 33-23.5 56.5T720-640Z"/></svg>`,
		areaChart: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M120-160v-520l160 120 200-280 200 160h160v520H120Zm200-120 160-220 280 218v-318H652L496-725 298-447l-98-73v144l120 96Z"/></svg>`,
		arrowDropDown: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-360 280-560h400L480-360Z"/></svg>`,
		arrowDropDownCircle: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m480-360 160-160H320l160 160Zm0 280q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-320Z"/></svg>`,
		arrowLeftAlt: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M400-240 160-480l240-240 56 58-142 142h486v80H314l142 142-56 58Z"/></svg>`,
		arrowOutward: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m256-240-56-56 384-384H240v-80h480v480h-80v-344L256-240Z"/></svg>`,
		arrowRightAlt: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m560-240-56-58 142-142H160v-80h486L504-662l56-58 240 240-240 240Z"/></svg>`,
		arrowSelectorTool: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m320-410 79-110h170L320-716v306ZM551-80 406-392 240-160v-720l560 440H516l144 309-109 51ZM399-520Z"/></svg>`,
		arrowTopLeft: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M680-160v-400H313l144 144-56 57-241-241 240-240 57 57-144 143h447v480h-80Z"/></svg>`,
		arrowUploadReady: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M170-228q-38-45-61-99T80-440h82q6 43 22 82.5t42 73.5l-56 56ZM80-520q8-59 30-113t60-99l56 56q-26 34-42 73.5T162-520H80ZM438-82q-59-6-112.5-28.5T226-170l56-58q35 26 74 43t82 23v80ZM284-732l-58-58q47-37 101-59.5T440-878v80q-43 6-82.5 23T284-732ZM518-82v-80q44-6 83.5-22.5T676-228l58 58q-47 38-101.5 60T518-82Zm160-650q-35-26-75-43t-83-23v-80q59 6 113.5 28.5T734-790l-56 58Zm112 504-56-56q26-34 42-73.5t22-82.5h82q-8 59-30 113t-60 99Zm8-292q-6-43-22-82.5T734-676l56-56q38 45 61 99t29 113h-82ZM441-280v-247L337-423l-56-57 200-200 200 200-57 56-103-103v247h-80Z"/></svg>`,
		article: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M280-280h280v-80H280v80Zm0-160h400v-80H280v80Zm0-160h400v-80H280v80Zm-80 480q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-560H200v560Zm0-560v560-560Z"/></svg>`,
		aspectRatio: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M560-280h200v-200h-80v120H560v80ZM200-480h80v-120h120v-80H200v200Zm-40 320q-33 0-56.5-23.5T80-240v-480q0-33 23.5-56.5T160-800h640q33 0 56.5 23.5T880-720v480q0 33-23.5 56.5T800-160H160Zm0-80h640v-480H160v480Zm0 0v-480 480Z"/></svg>`,
		assignmentLate: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-280q17 0 28.5-11.5T520-320q0-17-11.5-28.5T480-360q-17 0-28.5 11.5T440-320q0 17 11.5 28.5T480-280Zm-40-160h80v-240h-80v240ZM200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h168q13-36 43.5-58t68.5-22q38 0 68.5 22t43.5 58h168q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-560H200v560Zm280-590q13 0 21.5-8.5T510-820q0-13-8.5-21.5T480-850q-13 0-21.5 8.5T450-820q0 13 8.5 21.5T480-790ZM200-200v-560 560Z"/></svg>`,
		autoAwesomeMosaic: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M440-120H200q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h240v720Zm-80-80v-560H200v560h160Zm160-320v-320h240q33 0 56.5 23.5T840-760v240H520Zm80-80h160v-160H600v160Zm-80 480v-320h320v240q0 33-23.5 56.5T760-120H520Zm80-80h160v-160H600v160ZM360-480Zm240-120Zm0 240Z"/></svg>`,
		autoPlay: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M380-300v-360l280 180-280 180ZM480-40q-108 0-202.5-49.5T120-228v108H40v-240h240v80h-98q51 75 129.5 117.5T480-120q115 0 208.5-66T820-361l78 18q-45 136-160 219.5T480-40ZM42-520q7-67 32-128.5T143-762l57 57q-32 41-52 87.5T123-520H42Zm214-241-57-57q53-44 114-69.5T440-918v80q-51 5-97 25t-87 52Zm449 0q-41-32-87.5-52T520-838v-80q67 6 128.5 31T762-818l-57 57Zm133 241q-5-51-25-97.5T761-705l57-57q44 52 69 113.5T918-520h-80Z"/></svg>`,
		badge: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M160-80q-33 0-56.5-23.5T80-160v-440q0-33 23.5-56.5T160-680h200v-120q0-33 23.5-56.5T440-880h80q33 0 56.5 23.5T600-800v120h200q33 0 56.5 23.5T880-600v440q0 33-23.5 56.5T800-80H160Zm0-80h640v-440H600q0 33-23.5 56.5T520-520h-80q-33 0-56.5-23.5T360-600H160v440Zm80-80h240v-18q0-17-9.5-31.5T444-312q-20-9-40.5-13.5T360-330q-23 0-43.5 4.5T276-312q-17 8-26.5 22.5T240-258v18Zm320-60h160v-60H560v60Zm-200-60q25 0 42.5-17.5T420-420q0-25-17.5-42.5T360-480q-25 0-42.5 17.5T300-420q0 25 17.5 42.5T360-360Zm200-60h160v-60H560v60ZM440-600h80v-200h-80v200Zm40 220Z"/></svg>`,
		barChart: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M640-160v-280h160v280H640Zm-240 0v-640h160v640H400Zm-240 0v-440h160v440H160Z"/></svg>`,
		barChartAlt: Q`<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M4 16H11V20L4 20L4 16ZM4 10L20 10V14L4 14V10ZM4 4L15 4V8L4 8V4Z" fill="currentColor"/></svg>`,
		block: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q54 0 104-17.5t92-50.5L228-676q-33 42-50.5 92T160-480q0 134 93 227t227 93Zm252-124q33-42 50.5-92T800-480q0-134-93-227t-227-93q-54 0-104 17.5T284-732l448 448ZM480-480Z"/></svg>`,
		bolt: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m422-232 207-248H469l29-227-185 267h139l-30 208ZM320-80l40-280H160l360-520h80l-40 320h240L400-80h-80Zm151-390Z"/></svg>`,
		bottomPanelClose: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m480-500 160-160H320l160 160Zm280-340q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560ZM200-320v120h560v-120H200Zm560-80v-360H200v360h560Zm-560 80v120-120Z"/></svg>`,
		bubbleChart: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M580-120q-50 0-85-35t-35-85q0-50 35-85t85-35q50 0 85 35t35 85q0 50-35 85t-85 35Zm0-80q17 0 28.5-11.5T620-240q0-17-11.5-28.5T580-280q-17 0-28.5 11.5T540-240q0 17 11.5 28.5T580-200Zm80-200q-92 0-156-64t-64-156q0-92 64-156t156-64q92 0 156 64t64 156q0 92-64 156t-156 64Zm0-80q59 0 99.5-40.5T800-620q0-59-40.5-99.5T660-760q-59 0-99.5 40.5T520-620q0 59 40.5 99.5T660-480ZM280-240q-66 0-113-47t-47-113q0-66 47-113t113-47q66 0 113 47t47 113q0 66-47 113t-113 47Zm0-80q33 0 56.5-23.5T360-400q0-33-23.5-56.5T280-480q-33 0-56.5 23.5T200-400q0 33 23.5 56.5T280-320Zm300 80Zm80-380ZM280-400Z"/></svg>`,
		bugReport: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-200q66 0 113-47t47-113v-160q0-66-47-113t-113-47q-66 0-113 47t-47 113v160q0 66 47 113t113 47Zm-80-120h160v-80H400v80Zm0-160h160v-80H400v80Zm80 40Zm0 320q-65 0-120.5-32T272-240H160v-80h84q-3-20-3.5-40t-.5-40h-80v-80h80q0-20 .5-40t3.5-40h-84v-80h112q14-23 31.5-43t40.5-35l-64-66 56-56 86 86q28-9 57-9t57 9l88-86 56 56-66 66q23 15 41.5 34.5T688-640h112v80h-84q3 20 3.5 40t.5 40h80v80h-80q0 20-.5 40t-3.5 40h84v80H688q-32 56-87.5 88T480-120Z"/></svg>`,
		build: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M686-132 444-376q-20 8-40.5 12t-43.5 4q-100 0-170-70t-70-170q0-36 10-68.5t28-61.5l146 146 72-72-146-146q29-18 61.5-28t68.5-10q100 0 170 70t70 170q0 23-4 43.5T584-516l244 242q12 12 12 29t-12 29l-84 84q-12 12-29 12t-29-12Zm29-85 27-27-256-256q18-20 26-46.5t8-53.5q0-60-38.5-104.5T386-758l74 74q12 12 12 28t-12 28L332-500q-12 12-28 12t-28-12l-74-74q9 57 53.5 95.5T360-440q26 0 52-8t47-25l256 256ZM472-488Z"/></svg>`,
		buildCircle: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m620-284 56-56q6-6 6-14t-6-14L540-505q4-11 6-22t2-25q0-57-40.5-97.5T410-690q-17 0-34 4.5T343-673l94 94-56 56-94-94q-8 16-12.5 33t-4.5 34q0 57 40.5 97.5T408-412q13 0 24.5-2t22.5-6l137 136q6 6 14 6t14-6ZM480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-320Z"/></svg>`,
		calendarClock: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M200-640h560v-80H200v80Zm0 0v-80 80Zm0 560q-33 0-56.5-23.5T120-160v-560q0-33 23.5-56.5T200-800h40v-80h80v80h320v-80h80v80h40q33 0 56.5 23.5T840-720v227q-19-9-39-15t-41-9v-43H200v400h252q7 22 16.5 42T491-80H200Zm520 40q-83 0-141.5-58.5T520-240q0-83 58.5-141.5T720-440q83 0 141.5 58.5T920-240q0 83-58.5 141.5T720-40Zm67-105 28-28-75-75v-112h-40v128l87 87Z"/></svg>`,
		calendarMonth: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M200-80q-33 0-56.5-23.5T120-160v-560q0-33 23.5-56.5T200-800h40v-80h80v80h320v-80h80v80h40q33 0 56.5 23.5T840-720v560q0 33-23.5 56.5T760-80H200Zm0-80h560v-400H200v400Zm0-480h560v-80H200v80Zm0 0v-80 80Zm280 240q-17 0-28.5-11.5T440-440q0-17 11.5-28.5T480-480q17 0 28.5 11.5T520-440q0 17-11.5 28.5T480-400Zm-160 0q-17 0-28.5-11.5T280-440q0-17 11.5-28.5T320-480q17 0 28.5 11.5T360-440q0 17-11.5 28.5T320-400Zm320 0q-17 0-28.5-11.5T600-440q0-17 11.5-28.5T640-480q17 0 28.5 11.5T680-440q0 17-11.5 28.5T640-400ZM480-240q-17 0-28.5-11.5T440-280q0-17 11.5-28.5T480-320q17 0 28.5 11.5T520-280q0 17-11.5 28.5T480-240Zm-160 0q-17 0-28.5-11.5T280-280q0-17 11.5-28.5T320-320q17 0 28.5 11.5T360-280q0 17-11.5 28.5T320-240Zm320 0q-17 0-28.5-11.5T600-280q0-17 11.5-28.5T640-320q17 0 28.5 11.5T680-280q0 17-11.5 28.5T640-240Z"/></svg>`,
		capture: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M160-160q-33 0-56.5-23.5T80-240v-480q0-33 23.5-56.5T160-800h640q33 0 56.5 23.5T880-720v480q0 33-23.5 56.5T800-160H160Zm0-80h640v-480H160v480Zm80-80h480v-320H240v320Zm-80 80v-480 480Z"/></svg>`,
		changeHistory: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m80-160 400-640 400 640H80Zm144-80h512L480-650 224-240Zm256-205Z"/></svg>`,
		chatBubble: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M80-80v-720q0-33 23.5-56.5T160-880h640q33 0 56.5 23.5T880-800v480q0 33-23.5 56.5T800-240H240L80-80Zm126-240h594v-480H160v525l46-45Zm-46 0v-480 480Z"/></svg>`,
		chatDashed: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M80-480v-160h80v160H80Zm0 400v-320h80v125l46-45h114v80h-80L80-80Zm320-160v-80h160v80H400Zm240 0v-80h160v-80h80v80q0 33-23.5 56.5T800-240H640Zm160-240v-160h80v160h-80Zm0-239v-81H640v-80h160q33 0 56.5 23.5T880-800v81h-80Zm-400-81v-80h160v80H400ZM80-719v-81q0-33 23.5-56.5T160-880h160v80H160v81H80Z"/></svg>`,
		check: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M382-240 154-468l57-57 171 171 367-367 57 57-424 424Z"/></svg>`,
		checkBox: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m424-312 282-282-56-56-226 226-114-114-56 56 170 170ZM200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-560H200v560Zm0-560v560-560Z"/></svg>`,
		checkCircle: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m424-296 282-282-56-56-226 226-114-114-56 56 170 170Zm56 216q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-320Z"/></svg>`,
		checklist: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M222-200 80-342l56-56 85 85 170-170 56 57-225 226Zm0-320L80-662l56-56 85 85 170-170 56 57-225 226Zm298 240v-80h360v80H520Zm0-320v-80h360v80H520Z"/></svg>`,
		chevronLeft: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M560-240 320-480l240-240 56 56-184 184 184 184-56 56Z"/></svg>`,
		chevronRight: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M504-480 320-664l56-56 240 240-240 240-56-56 184-184Z"/></svg>`,
		chrome: Q`<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 63 63"><linearGradient id="a" gradientUnits="userSpaceOnUse" x1="34.9087" x2="7.63224" y1="61.029" y2="13.7847"><stop offset="0" stop-color="#1e8e3e"/><stop offset="1" stop-color="#34a853"/></linearGradient><linearGradient id="b" gradientUnits="userSpaceOnUse" x1="26.9043" x2="54.1808" y1="63.0788" y2="15.8345"><stop offset="0" stop-color="#fcc934"/><stop offset="1" stop-color="#fbbc04"/></linearGradient><linearGradient id="c" gradientUnits="userSpaceOnUse" x1="4.22145" x2="58.7745" y1="19.6884" y2="19.6884"><stop offset="0" stop-color="#d93025"/><stop offset="1" stop-color="#ea4335"/></linearGradient><path d="m31.499 47.2466c8.6985 0 15.75-7.0515 15.75-15.75s-7.0515-15.75-15.75-15.75-15.75 7.0515-15.75 15.75 7.0515 15.75 15.75 15.75z" fill="#fff"/><path d="m17.8591 39.3751-13.63772-23.6212c-2.76527 4.788-4.22118922 10.2197-4.22137998 15.7489s1.45535998 10.961 4.22028998 15.7492c2.76494 4.7882 6.74181 8.7641 11.53071 11.5279 4.7889 2.7637 10.221 4.2179 15.7502 4.2164l13.6377-23.6212v-.0041c-1.3813 2.3954-3.369 4.3848-5.7632 5.7681s-5.1104 2.1118-7.8755 2.1122-5.4816-.7272-7.8762-2.1099c-2.3945-1.3826-4.3829-3.3714-5.7649-5.7663z" fill="url(#a)"/><path d="m45.1379 39.3741-13.6376 23.6212c5.5292.0008 10.9611-1.4542 15.7496-4.2187 4.7885-2.7644 8.7648-6.7408 11.5291-11.5294 2.7642-4.7887 4.219-10.2207 4.2181-15.7499-.001-5.5292-1.4577-10.9606-4.2237-15.7483h-27.2754l-.0034.0021c2.7651-.0014 5.4818.7254 7.8769 2.1071 2.3951 1.3818 4.3841 3.3698 5.767 5.7643 1.3829 2.3944 2.1109 5.1108 2.1109 7.8758-.0001 2.7651-.7283 5.4814-2.1113 7.8758z" fill="url(#b)"/><path d="m31.499 43.9688c6.8863 0 12.4688-5.5825 12.4688-12.4688s-5.5825-12.4688-12.4688-12.4688-12.4687 5.5825-12.4687 12.4688 5.5824 12.4688 12.4687 12.4688z" fill="#1a73e8"/><path d="m31.4991 15.75h27.2754c-2.764-4.7888-6.74-8.76553-11.5283-11.53029-4.7883-2.76475-10.2202-4.22010235-15.7494-4.21970992s-10.9608 1.45650992-15.7487 4.22194992c-4.788 2.76543-8.76341 6.74275-11.52666 11.53185l13.63766 23.6212.0035.0019c-1.3837-2.394-2.1127-5.11-2.1136-7.8751s.7264-5.4817 2.1086-7.8765c1.3821-2.3948 3.3706-4.3835 5.7652-5.7659 2.3947-1.3825 5.1112-2.11 7.8763-2.1094z" fill="url(#c)"/></svg>`,
		close: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m256-200-56-56 224-224-224-224 56-56 224 224 224-224 56 56-224 224 224 224-56 56-224-224-224 224Z"/></svg>`,
		code: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M320-240 80-480l240-240 57 57-184 184 183 183-56 56Zm320 0-57-57 184-184-183-183 56-56 240 240-240 240Z"/></svg>`,
		computer: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M40-120v-80h880v80H40Zm120-120q-33 0-56.5-23.5T80-320v-440q0-33 23.5-56.5T160-840h640q33 0 56.5 23.5T880-760v440q0 33-23.5 56.5T800-240H160Zm0-80h640v-440H160v440Zm0 0v-440 440Z"/></svg>`,
		construction: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M756-120 537-339l84-84 219 219-84 84Zm-552 0-84-84 276-276-68-68-28 28-51-51v82l-28 28-121-121 28-28h82l-50-50 142-142q20-20 43-29t47-9q24 0 47 9t43 29l-92 92 50 50-28 28 68 68 90-90q-4-11-6.5-23t-2.5-24q0-59 40.5-99.5T701-841q15 0 28.5 3t27.5 9l-99 99 72 72 99-99q7 14 9.5 27.5T841-701q0 59-40.5 99.5T701-561q-12 0-24-2t-23-7L204-120Z"/></svg>`,
		contentCopy: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M360-240q-33 0-56.5-23.5T280-320v-480q0-33 23.5-56.5T360-880h360q33 0 56.5 23.5T800-800v480q0 33-23.5 56.5T720-240H360Zm0-80h360v-480H360v480ZM200-80q-33 0-56.5-23.5T120-160v-560h80v560h440v80H200Zm160-240v-480 480Z"/></svg>`,
		contentPaste: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h167q11-35 43-57.5t70-22.5q40 0 71.5 22.5T594-840h166q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-560h-80v120H280v-120h-80v560Zm280-560q17 0 28.5-11.5T520-800q0-17-11.5-28.5T480-840q-17 0-28.5 11.5T440-800q0 17 11.5 28.5T480-760Z"/></svg>`,
		copilot: Q`<svg viewBox="0 0 34 32" fill="none" xmlns="http://www.w3.org/2000/svg"><g clip-path="url(#clip0_119_234)"><path d="M9.34764 6.65138L2.4664 13.5326C1.10424 14.8948 1.10424 17.1033 2.4664 18.4654L9.34766 25.3467C10.7098 26.7088 12.9183 26.7088 14.2805 25.3467L21.1617 18.4654C22.5239 17.1033 22.5239 14.8948 21.1617 13.5326L14.2804 6.65138C12.9183 5.28923 10.7098 5.28923 9.34764 6.65138Z" fill="#00B4F0"></path><path d="M19.7195 6.65307L12.8382 13.5343C11.4761 14.8965 11.4761 17.105 12.8382 18.4671L19.7195 25.3484C21.0816 26.7105 23.2901 26.7105 24.6523 25.3484L31.5335 18.4671C32.8957 17.105 32.8957 14.8965 31.5335 13.5343L24.6523 6.65307C23.2901 5.29092 21.0816 5.29092 19.7195 6.65307Z" fill="#FF707A"></path><path d="M21.1677 13.5343L16.9821 9.34863L12.7965 13.5343C12.1426 14.1884 11.7753 15.0754 11.7753 16.0003C11.7753 16.9252 12.1426 17.8122 12.7965 18.4663L16.9821 22.6519L21.1677 18.4663C21.8216 17.8122 22.189 16.9252 22.189 16.0003C22.189 15.0754 21.8216 14.1884 21.1677 13.5343Z" fill="#5748FF"></path></g><defs><clipPath id="clip0_119_234"><rect width="34" height="23.6298" fill="white" transform="translate(0 4.18506)"></rect></clipPath></defs></svg>`,
		conversionPath: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M760-120q-39 0-70-22.5T647-200H440q-66 0-113-47t-47-113q0-66 47-113t113-47h80q33 0 56.5-23.5T600-600q0-33-23.5-56.5T520-680H313q-13 35-43.5 57.5T200-600q-50 0-85-35t-35-85q0-50 35-85t85-35q39 0 69.5 22.5T313-760h207q66 0 113 47t47 113q0 66-47 113t-113 47h-80q-33 0-56.5 23.5T360-360q0 33 23.5 56.5T440-280h207q13-35 43.5-57.5T760-360q50 0 85 35t35 85q0 50-35 85t-85 35ZM228.5-691.5Q240-703 240-720t-11.5-28.5Q217-760 200-760t-28.5 11.5Q160-737 160-720t11.5 28.5Q183-680 200-680t28.5-11.5Z"/></svg>`,
		darkMode: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-120q-150 0-255-105T120-480q0-150 105-255t255-105q14 0 27.5 1t26.5 3q-41 29-65.5 75.5T444-660q0 90 63 153t153 63q55 0 101-24.5t75-65.5q2 13 3 26.5t1 27.5q0 150-105 255T480-120Zm0-80q88 0 158-48.5T740-375q-20 5-40 8t-40 3q-123 0-209.5-86.5T364-660q0-20 3-40t8-40q-78 32-126.5 102T200-480q0 116 82 198t198 82Zm-10-270Z"/></svg>`,
		dashboard: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M520-600v-240h320v240H520ZM120-440v-400h320v400H120Zm400 320v-400h320v400H520Zm-400 0v-240h320v240H120Zm80-400h160v-240H200v240Zm400 320h160v-240H600v240Zm0-480h160v-80H600v80ZM200-200h160v-80H200v80Zm160-320Zm240-160Zm0 240ZM360-280Z"/></svg>`,
		dashboardCustomize: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M120-840h320v320H120v-320Zm80 80v160-160Zm320-80h320v320H520v-320Zm80 80v160-160ZM120-440h320v320H120v-320Zm80 80v160-160Zm440-80h80v120h120v80H720v120h-80v-120H520v-80h120v-120Zm-40-320v160h160v-160H600Zm-400 0v160h160v-160H200Zm0 400v160h160v-160H200Z"/></svg>`,
		database: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-120q-151 0-255.5-46.5T120-280v-400q0-66 105.5-113T480-840q149 0 254.5 47T840-680v400q0 67-104.5 113.5T480-120Zm0-479q89 0 179-25.5T760-679q-11-29-100.5-55T480-760q-91 0-178.5 25.5T200-679q14 30 101.5 55T480-599Zm0 199q42 0 81-4t74.5-11.5q35.5-7.5 67-18.5t57.5-25v-120q-26 14-57.5 25t-67 18.5Q600-528 561-524t-81 4q-42 0-82-4t-75.5-11.5Q287-543 256-554t-56-25v120q25 14 56 25t66.5 18.5Q358-408 398-404t82 4Zm0 200q46 0 93.5-7t87.5-18.5q40-11.5 67-26t32-29.5v-98q-26 14-57.5 25t-67 18.5Q600-328 561-324t-81 4q-42 0-82-4t-75.5-11.5Q287-343 256-354t-56-25v99q5 15 31.5 29t66.5 25.5q40 11.5 88 18.5t94 7Z"/></svg>`,
		dataObject: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M560-160v-80h120q17 0 28.5-11.5T720-280v-80q0-38 22-69t58-44v-14q-36-13-58-44t-22-69v-80q0-17-11.5-28.5T680-720H560v-80h120q50 0 85 35t35 85v80q0 17 11.5 28.5T840-560h40v160h-40q-17 0-28.5 11.5T800-360v80q0 50-35 85t-85 35H560Zm-280 0q-50 0-85-35t-35-85v-80q0-17-11.5-28.5T120-400H80v-160h40q17 0 28.5-11.5T160-600v-80q0-50 35-85t85-35h120v80H280q-17 0-28.5 11.5T240-680v80q0 38-22 69t-58 44v14q36 13 58 44t22 69v80q0 17 11.5 28.5T280-240h120v80H280Z"/></svg>`,
		delete: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M280-120q-33 0-56.5-23.5T200-200v-520h-40v-80h200v-40h240v40h200v80h-40v520q0 33-23.5 56.5T680-120H280Zm400-600H280v520h400v-520ZM360-280h80v-360h-80v360Zm160 0h80v-360h-80v360ZM280-720v520-520Z"/></svg>`,
		deleteSweep: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M600-240v-80h160v80H600Zm0-320v-80h280v80H600Zm0 160v-80h240v80H600ZM120-640H80v-80h160v-60h160v60h160v80h-40v360q0 33-23.5 56.5T440-200H200q-33 0-56.5-23.5T120-280v-360Zm80 0v360h240v-360H200Zm0 0v360-360Z"/></svg>`,
		deployedCube: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M440-183v-274L200-596v274l240 139Zm80 0 240-139v-274L520-457v274Zm-40-343 237-137-237-137-237 137 237 137ZM160-252q-19-11-29.5-29T120-321v-318q0-22 10.5-40t29.5-29l280-161q19-11 40-11t40 11l280 161q19 11 29.5 29t10.5 40v318q0 22-10.5 40T800-252L520-91q-19 11-40 11t-40-11L160-252Zm320-228Z"/></svg>`,
		developerModeTv: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M80-680v-80q0-33 23.5-56.5T160-840h640q33 0 56.5 23.5T880-760v80h-80v-80H160v80H80Zm240 560v-80H160q-33 0-56.5-23.5T80-280v-80h80v80h640v-80h80v80q0 33-23.5 56.5T800-200H640v80H320Zm160-400Zm-288 0 104-104-56-56L80-520l160 160 56-56-104-104Zm576 0L664-416l56 56 160-160-160-160-56 56 104 104Z"/></svg>`,
		dockToBottom: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-200v120h560v-120H200Zm0-80h560v-360H200v360Zm0 80v120-120Z"/></svg>`,
		dockToLeft: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm440-80h120v-560H640v560Zm-80 0v-560H200v560h360Zm80 0h120-120Z"/></svg>`,
		dockToRight: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm120-80v-560H200v560h120Zm80 0h360v-560H400v560Zm-80 0H200h120Z"/></svg>`,
		doneAll: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M268-240 42-466l57-56 170 170 56 56-57 56Zm226 0L268-466l56-57 170 170 368-368 56 57-424 424Zm0-226-57-56 198-198 57 56-198 198Z"/></svg>`,
		doNotDisturbOn: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M280-440h400v-80H280v80ZM480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Z"/></svg>`,
		draft: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M240-80q-33 0-56.5-23.5T160-160v-640q0-33 23.5-56.5T240-880h320l240 240v480q0 33-23.5 56.5T720-80H240Zm280-520v-200H240v640h480v-440H520ZM240-800v200-200 640-640Z"/></svg>`,
		draw: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M160-120v-170l527-526q12-12 27-18t30-6q16 0 30.5 6t25.5 18l56 56q12 11 18 25.5t6 30.5q0 15-6 30t-18 27L330-120H160Zm80-80h56l393-392-28-29-29-28-392 393v56Zm560-503-57-57 57 57Zm-139 82-29-28 57 57-28-29ZM560-120q74 0 137-37t63-103q0-36-19-62t-51-45l-59 59q23 10 36 22t13 26q0 23-36.5 41.5T560-200q-17 0-28.5 11.5T520-160q0 17 11.5 28.5T560-120ZM183-426l60-60q-20-8-31.5-16.5T200-520q0-12 18-24t76-37q88-38 117-69t29-70q0-55-44-87.5T280-840q-45 0-80.5 16T145-785q-11 13-9 29t15 26q13 11 29 9t27-13q14-14 31-20t42-6q41 0 60.5 12t19.5 28q0 14-17.5 25.5T262-654q-80 35-111 63.5T120-520q0 32 17 54.5t46 39.5Z"/></svg>`,
		dragIndicator: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M360-160q-33 0-56.5-23.5T280-240q0-33 23.5-56.5T360-320q33 0 56.5 23.5T440-240q0 33-23.5 56.5T360-160Zm240 0q-33 0-56.5-23.5T520-240q0-33 23.5-56.5T600-320q33 0 56.5 23.5T680-240q0 33-23.5 56.5T600-160ZM360-400q-33 0-56.5-23.5T280-480q0-33 23.5-56.5T360-560q33 0 56.5 23.5T440-480q0 33-23.5 56.5T360-400Zm240 0q-33 0-56.5-23.5T520-480q0-33 23.5-56.5T600-560q33 0 56.5 23.5T680-480q0 33-23.5 56.5T600-400ZM360-640q-33 0-56.5-23.5T280-720q0-33 23.5-56.5T360-800q33 0 56.5 23.5T440-720q0 33-23.5 56.5T360-640Zm240 0q-33 0-56.5-23.5T520-720q0-33 23.5-56.5T600-800q33 0 56.5 23.5T680-720q0 33-23.5 56.5T600-640Z"/></svg>`,
		eclipse: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 470 111"><path fill="#2C2255" d="M30.054 69.273H13.31c2.217 7.605 6.331 14.422 12.354 20.445 9.602 9.604 21.148 14.398 34.65 14.398 2.699 0 5.314-.201 7.854-.584 10.17-1.535 19.074-6.133 26.703-13.814 6.062-6.021 10.205-12.84 12.439-20.445h-6.795h-9.936H30.054z"/><path fill="#2C2255" d="M20.821 46.531h-8.747c-.32 1.922-.538 3.884-.637 5.896h10.396h5.215h75.275h6.871c-.1-2.012-.318-3.974-.641-5.896"/><path fill="#2C2255" d="M11.437 57.902c.099 2.013.316 3.975.637 5.896h9.094h6.893h73.648h6.846c.322-1.921.542-3.883.642-5.896"/><path fill="#2C2255" d="M107.312 41.055c-2.232-7.626-6.376-14.474-12.441-20.54-7.609-7.608-16.488-12.174-26.625-13.71a50.123 50.123 0 00-7.932-.593c-13.502 0-25.049 4.769-34.65 14.303-6.025 6.066-10.141 12.914-12.357 20.54"/><path fill="#F7941E" d="M8.53 55.166c0-25.824 19.395-47.281 44.626-51.055-.626-.023-1.255-.049-1.887-.049C23.955 4.062 1 26.943 1 55.166c0 28.224 22.954 51.103 51.269 51.103.634 0 1.263-.023 1.891-.047C27.925 102.448 8.53 80.991 8.53 55.166z"/><linearGradient id="a" x1="131.315" x2="131.315" y1="434.424" y2="360.789" gradientTransform="translate(-71 -323.663)" gradientUnits="userSpaceOnUse"><stop offset=".303" stop-color="#473788"/><stop offset=".872" stop-color="#2C2255"/></linearGradient><path fill="url(#a)" d="M93.583 52.426c-.164-2.021-.504-3.992-1.012-5.896h-64.51c-.508 1.902-.849 3.874-1.013 5.896h66.535z"/><linearGradient id="b" x1="131.316" x2="131.316" y1="434.425" y2="360.79" gradientTransform="translate(-71 -323.663)" gradientUnits="userSpaceOnUse"><stop offset=".303" stop-color="#473788"/><stop offset=".872" stop-color="#2C2255"/></linearGradient><path fill="url(#b)" d="M93.583 57.902H27.049c.164 2.021.503 3.991 1.012 5.896h64.511c.508-1.904.847-3.876 1.011-5.896z"/><linearGradient id="c" x1="131.316" x2="131.316" y1="434.422" y2="360.793" gradientTransform="translate(-71 -323.663)" gradientUnits="userSpaceOnUse"><stop offset=".303" stop-color="#473788"/><stop offset=".863" stop-color="#2C2255"/></linearGradient><path fill="url(#c)" d="M60.316 88.554c13.397 0 24.945-7.893 30.263-19.281H30.053c5.318 11.388 16.866 19.281 30.263 19.281z"/><path fill="#FFF" d="M22.118 57.902h4.884h66.706h8.763h6.657c.046-.871.072-1.748.072-2.632 0-.955-.035-1.901-.089-2.845h-6.641h-8.763H27.001h-5.229H11.437c-.054.942-.089 1.89-.089 2.845 0 .884.026 1.761.072 2.632h10.698z"/><path fill="#FFF" d="M29.858 41.055H13.306c-.544 1.783-.895 3.612-1.232 5.477h8.525h7.259h64.679h9.16h6.547c-.338-1.863-.773-3.692-1.316-5.477"/><path fill="#FFF" d="M101.956 63.797h-9.16H28.118h-6.912h-9.133c.328 1.863.701 3.691 1.236 5.476h16.807h60.682h9.962h6.488c.534-1.783.965-3.611 1.294-5.476h-6.586z"/></svg>`,
		edge: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 256 256"><defs><radialGradient id="b" cx="161.8" cy="68.9" r="95.4" gradientTransform="matrix(1 0 0 -.95 0 248.8)" gradientUnits="userSpaceOnUse"><stop offset=".7" stop-opacity="0"/><stop offset=".9" stop-opacity=".5"/><stop offset="1"/></radialGradient><radialGradient id="d" cx="-340.3" cy="63" r="143.2" gradientTransform="matrix(.15 -.99 -.8 -.12 176.6 -125.4)" gradientUnits="userSpaceOnUse"><stop offset=".8" stop-opacity="0"/><stop offset=".9" stop-opacity=".5"/><stop offset="1"/></radialGradient><radialGradient id="e" cx="113.4" cy="570.2" r="202.4" gradientTransform="matrix(-.04 1 2.13 .08 -1179.5 -106.7)" gradientUnits="userSpaceOnUse"><stop offset="0" stop-color="#35c1f1"/><stop offset=".1" stop-color="#34c1ed"/><stop offset=".2" stop-color="#2fc2df"/><stop offset=".3" stop-color="#2bc3d2"/><stop offset=".7" stop-color="#36c752"/></radialGradient><radialGradient id="f" cx="376.5" cy="568" r="97.3" gradientTransform="matrix(.28 .96 .78 -.23 -303.8 -148.5)" gradientUnits="userSpaceOnUse"><stop offset="0" stop-color="#66eb6e"/><stop offset="1" stop-color="#66eb6e" stop-opacity="0"/></radialGradient><linearGradient id="a" x1="63.3" y1="84" x2="241.7" y2="84" gradientTransform="matrix(1 0 0 -1 0 266)" gradientUnits="userSpaceOnUse"><stop offset="0" stop-color="#0c59a4"/><stop offset="1" stop-color="#114a8b"/></linearGradient><linearGradient id="c" x1="157.3" y1="161.4" x2="46" y2="40.1" gradientTransform="matrix(1 0 0 -1 0 266)" gradientUnits="userSpaceOnUse"><stop offset="0" stop-color="#1b9de2"/><stop offset=".2" stop-color="#1595df"/><stop offset=".7" stop-color="#0680d7"/><stop offset="1" stop-color="#0078d4"/></linearGradient></defs><path d="M235.7 195.5a93.7 93.7 0 0 1-10.6 4.7 101.9 101.9 0 0 1-35.9 6.4c-47.3 0-88.5-32.5-88.5-74.3a31.5 31.5 0 0 1 16.4-27.3c-42.8 1.8-53.8 46.4-53.8 72.5 0 74 68.1 81.4 82.8 81.4 7.9 0 19.8-2.3 27-4.6l1.3-.4a128.3 128.3 0 0 0 66.6-52.8 4 4 0 0 0-5.3-5.6Z" transform="translate(-4.6 -5)" style="fill:url(#a)"/><path d="M235.7 195.5a93.7 93.7 0 0 1-10.6 4.7 101.9 101.9 0 0 1-35.9 6.4c-47.3 0-88.5-32.5-88.5-74.3a31.5 31.5 0 0 1 16.4-27.3c-42.8 1.8-53.8 46.4-53.8 72.5 0 74 68.1 81.4 82.8 81.4 7.9 0 19.8-2.3 27-4.6l1.3-.4a128.3 128.3 0 0 0 66.6-52.8 4 4 0 0 0-5.3-5.6Z" transform="translate(-4.6 -5)" style="isolation:isolate;opacity:.35;fill:url(#b)"/><path d="M110.3 246.3A79.2 79.2 0 0 1 87.6 225a80.7 80.7 0 0 1 29.5-120c3.2-1.5 8.5-4.1 15.6-4a32.4 32.4 0 0 1 25.7 13 31.9 31.9 0 0 1 6.3 18.7c0-.2 24.5-79.6-80-79.6-43.9 0-80 41.6-80 78.2a130.2 130.2 0 0 0 12.1 56 128 128 0 0 0 156.4 67 75.5 75.5 0 0 1-62.8-8Z" transform="translate(-4.6 -5)" style="fill:url(#c)"/><path d="M110.3 246.3A79.2 79.2 0 0 1 87.6 225a80.7 80.7 0 0 1 29.5-120c3.2-1.5 8.5-4.1 15.6-4a32.4 32.4 0 0 1 25.7 13 31.9 31.9 0 0 1 6.3 18.7c0-.2 24.5-79.6-80-79.6-43.9 0-80 41.6-80 78.2a130.2 130.2 0 0 0 12.1 56 128 128 0 0 0 156.4 67 75.5 75.5 0 0 1-62.8-8Z" transform="translate(-4.6 -5)" style="opacity:.41;fill:url(#d);isolation:isolate"/><path d="M157 153.8c-.9 1-3.4 2.5-3.4 5.6 0 2.6 1.7 5.2 4.8 7.3 14.3 10 41.4 8.6 41.5 8.6a59.6 59.6 0 0 0 30.3-8.3 61.4 61.4 0 0 0 30.4-52.9c.3-22.4-8-37.3-11.3-43.9C228 28.8 182.3 5 132.6 5a128 128 0 0 0-128 126.2c.5-36.5 36.8-66 80-66 3.5 0 23.5.3 42 10a72.6 72.6 0 0 1 30.9 29.3c6.1 10.6 7.2 24.1 7.2 29.5s-2.7 13.3-7.8 19.9Z" transform="translate(-4.6 -5)" style="fill:url(#e)"/><path d="M157 153.8c-.9 1-3.4 2.5-3.4 5.6 0 2.6 1.7 5.2 4.8 7.3 14.3 10 41.4 8.6 41.5 8.6a59.6 59.6 0 0 0 30.3-8.3 61.4 61.4 0 0 0 30.4-52.9c.3-22.4-8-37.3-11.3-43.9C228 28.8 182.3 5 132.6 5a128 128 0 0 0-128 126.2c.5-36.5 36.8-66 80-66 3.5 0 23.5.3 42 10a72.6 72.6 0 0 1 30.9 29.3c6.1 10.6 7.2 24.1 7.2 29.5s-2.7 13.3-7.8 19.9Z" transform="translate(-4.6 -5)" style="fill:url(#f)"/></svg>`,
		edit: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M200-200h57l391-391-57-57-391 391v57Zm-80 80v-170l528-527q12-11 26.5-17t30.5-6q16 0 31 6t26 18l55 56q12 11 17.5 26t5.5 30q0 16-5.5 30.5T817-647L290-120H120Zm640-584-56-56 56 56Zm-141 85-28-29 57 57-29-28Z"/></svg>`,
		editDocument: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M560-80v-123l221-220q9-9 20-13t22-4q12 0 23 4.5t20 13.5l37 37q8 9 12.5 20t4.5 22q0 11-4 22.5T903-300L683-80H560Zm300-263-37-37 37 37ZM620-140h38l121-122-18-19-19-18-122 121v38ZM240-80q-33 0-56.5-23.5T160-160v-640q0-33 23.5-56.5T240-880h320l240 240v120h-80v-80H520v-200H240v640h240v80H240Zm280-400Zm241 199-19-18 37 37-18-19Z"/></svg>`,
		editLocation: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M340-420h58l156-157-57-57-157 156v58Zm243-186 28-28q5-5 5-10.5t-5-10.5l-36-36q-5-5-10.5-5t-10.5 5l-28 28 57 57ZM480-186q122-112 181-203.5T720-552q0-109-69.5-178.5T480-800q-101 0-170.5 69.5T240-552q0 71 59 162.5T480-186Zm0 106Q319-217 239.5-334.5T160-552q0-150 96.5-239T480-880q127 0 223.5 89T800-552q0 100-79.5 217.5T480-80Zm0-480Z"/></svg>`,
		editLocationAlt: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-80Q319-217 239.5-334.5T160-552q0-150 96.5-239T480-880q27 0 53.5 4.5T585-863l-65 66q-10-2-19.5-2.5T480-800q-101 0-170.5 69.5T240-552q0 71 59 162.5T480-186q122-112 181-203.5T720-552q0-12-1-24t-3-23l66-66q9 26 13.5 54t4.5 59q0 100-79.5 217.5T480-80Zm0-472Zm254-254-46-46-248 248v84h84l248-248-38-38Zm66 10 28-28q11-11 11-28t-11-28l-28-28q-11-11-28-11t-28 11l-28 28 84 84Z"/></svg>`,
		editSquare: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h357l-80 80H200v560h560v-278l80-80v358q0 33-23.5 56.5T760-120H200Zm280-360ZM360-360v-170l367-367q12-12 27-18t30-6q16 0 30.5 6t26.5 18l56 57q11 12 17 26.5t6 29.5q0 15-5.5 29.5T897-728L530-360H360Zm481-424-56-56 56 56ZM440-440h56l232-232-28-28-29-28-231 231v57Zm260-260-29-28 29 28 28 28-28-28Z"/></svg>`,
		emojiPeople: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M360-80v-529q-91-24-145.5-100.5T160-880h80q0 83 53.5 141.5T430-680h100q30 0 56 11t47 32l181 181-56 56-158-158v478h-80v-240h-80v240h-80Zm120-640q-33 0-56.5-23.5T400-800q0-33 23.5-56.5T480-880q33 0 56.5 23.5T560-800q0 33-23.5 56.5T480-720Z"/></svg>`,
		error: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-280q17 0 28.5-11.5T520-320q0-17-11.5-28.5T480-360q-17 0-28.5 11.5T440-320q0 17 11.5 28.5T480-280Zm-40-160h80v-240h-80v240Zm40 360q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-320Z"/></svg>`,
		exclamation: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M440-400v-360h80v360h-80Zm0 200v-80h80v80h-80Z"/></svg>`,
		expand: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M160-80v-80h640v80H160Zm320-120L320-360l56-56 64 62v-252l-64 62-56-56 160-160 160 160-56 56-64-62v252l64-62 56 56-160 160ZM160-800v-80h640v80H160Z"/></svg>`,
		expandCircleDown: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m480-340 180-180-57-56-123 123-123-123-57 56 180 180Zm0 260q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-320Z"/></svg>`,
		expansionPanels: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m480-240 160-160-57-57-103 103-103-103-57 57 160 160ZM377-503l103-103 103 103 57-57-160-160-160 160 57 57ZM200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-560H200v560Zm0-560v560-560Z"/></svg>`,
		experiment: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M200-120q-51 0-72.5-45.5T138-250l222-270v-240h-40q-17 0-28.5-11.5T280-800q0-17 11.5-28.5T320-840h320q17 0 28.5 11.5T680-800q0 17-11.5 28.5T640-760h-40v240l222 270q32 39 10.5 84.5T760-120H200Zm80-120h400L544-400H416L280-240Zm-80 40h560L520-492v-268h-80v268L200-200Zm280-280Z"/></svg>`,
		feedback: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-360q17 0 28.5-11.5T520-400q0-17-11.5-28.5T480-440q-17 0-28.5 11.5T440-400q0 17 11.5 28.5T480-360Zm-40-160h80v-240h-80v240ZM80-80v-720q0-33 23.5-56.5T160-880h640q33 0 56.5 23.5T880-800v480q0 33-23.5 56.5T800-240H240L80-80Zm126-240h594v-480H160v525l46-45Zm-46 0v-480 480Z"/></svg>`,
		fileCopy: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M760-200H320q-33 0-56.5-23.5T240-280v-560q0-33 23.5-56.5T320-920h280l240 240v400q0 33-23.5 56.5T760-200ZM560-640v-200H320v560h440v-360H560ZM160-40q-33 0-56.5-23.5T80-120v-560h80v560h440v80H160Zm160-800v200-200 560-560Z"/></svg>`,
		fileExport: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-480ZM202-65l-56-57 118-118h-90v-80h226v226h-80v-89L202-65Zm278-15v-80h240v-440H520v-200H240v400h-80v-400q0-33 23.5-56.5T240-880h320l240 240v480q0 33-23.5 56.5T720-80H480Z"/></svg>`,
		fileOpen: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M240-80q-33 0-56.5-23.5T160-160v-640q0-33 23.5-56.5T240-880h320l240 240v240h-80v-200H520v-200H240v640h360v80H240Zm638 15L760-183v89h-80v-226h226v80h-90l118 118-56 57Zm-638-95v-640 640Z"/></svg>`,
		fileSave: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m720-120 160-160-56-56-64 64v-167h-80v167l-64-64-56 56 160 160ZM560 0v-80h320V0H560ZM240-160q-33 0-56.5-23.5T160-240v-560q0-33 23.5-56.5T240-880h280l240 240v121h-80v-81H480v-200H240v560h240v80H240Zm0-80v-560 560Z"/></svg>`,
		filterAlt: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M440-160q-17 0-28.5-11.5T400-200v-240L168-736q-15-20-4.5-42t36.5-22h560q26 0 36.5 22t-4.5 42L560-440v240q0 17-11.5 28.5T520-160h-80Zm40-308 198-252H282l198 252Zm0 0Z"/></svg>`,
		findInPage: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m590-160 80 80H240q-33 0-56.5-23.5T160-160v-640q0-33 23.5-56.5T240-880h360l200 240v480q0 20-8.5 36.5T768-96L560-302q-17 11-37 16.5t-43 5.5q-66 0-113-47t-47-113q0-66 47-113t113-47q66 0 113 47t47 113q0 23-5.5 43T618-360l102 104v-356L562-800H240v640h350Zm-53.5-223.5Q560-407 560-440t-23.5-56.5Q513-520 480-520t-56.5 23.5Q400-473 400-440t23.5 56.5Q447-360 480-360t56.5-23.5ZM480-440Zm0 0Z"/></svg>`,
		firefox: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512"><defs><linearGradient id="a" x1="446.9" y1="76.85" x2="47.94" y2="461.77" gradientUnits="userSpaceOnUse"><stop offset=".05" stop-color="#fff44f"/><stop offset=".11" stop-color="#ffe847"/><stop offset=".22" stop-color="#ffc830"/><stop offset=".37" stop-color="#ff980e"/><stop offset=".4" stop-color="#ff8b16"/><stop offset=".46" stop-color="#ff672a"/><stop offset=".53" stop-color="#ff3647"/><stop offset=".7" stop-color="#e31587"/></linearGradient><radialGradient id="b" cx="428.46" cy="55.06" r="501.01" gradientUnits="userSpaceOnUse"><stop offset=".13" stop-color="#ffbd4f"/><stop offset=".19" stop-color="#ffac31"/><stop offset=".25" stop-color="#ff9d17"/><stop offset=".28" stop-color="#ff980e"/><stop offset=".4" stop-color="#ff563b"/><stop offset=".47" stop-color="#ff3750"/><stop offset=".71" stop-color="#f5156c"/><stop offset=".78" stop-color="#eb0878"/><stop offset=".86" stop-color="#e50080"/></radialGradient><radialGradient id="c" cx="245.39" cy="259.88" r="501.01" gradientUnits="userSpaceOnUse"><stop offset=".3" stop-color="#960e18"/><stop offset=".35" stop-color="#b11927" stop-opacity=".74"/><stop offset=".43" stop-color="#db293d" stop-opacity=".34"/><stop offset=".5" stop-color="#f5334b" stop-opacity=".09"/><stop offset=".53" stop-color="#ff3750" stop-opacity="0"/></radialGradient><radialGradient id="d" cx="305.8" cy="-58.64" r="362.96" gradientUnits="userSpaceOnUse"><stop offset=".13" stop-color="#fff44f"/><stop offset=".25" stop-color="#ffdc3e"/><stop offset=".51" stop-color="#ff9d12"/><stop offset=".53" stop-color="#ff980e"/></radialGradient><radialGradient id="e" cx="189.98" cy="390.75" r="238.55" gradientUnits="userSpaceOnUse"><stop offset=".35" stop-color="#3a8ee6"/><stop offset=".47" stop-color="#5c79f0"/><stop offset=".67" stop-color="#9059ff"/><stop offset="1" stop-color="#c139e6"/></radialGradient><radialGradient id="f" cx="252.16" cy="201.25" r="126.48" gradientTransform="matrix(.97 -.24 .28 1.14 -48.36 31.43)" gradientUnits="userSpaceOnUse"><stop offset=".21" stop-color="#9059ff" stop-opacity="0"/><stop offset=".28" stop-color="#8c4ff3" stop-opacity=".06"/><stop offset=".75" stop-color="#7716a8" stop-opacity=".45"/><stop offset=".97" stop-color="#6e008b" stop-opacity=".6"/></radialGradient><radialGradient id="g" cx="239.06" cy="34.56" r="171.62" gradientUnits="userSpaceOnUse"><stop offset="0" stop-color="#ffe226"/><stop offset=".12" stop-color="#ffdb27"/><stop offset=".3" stop-color="#ffc82a"/><stop offset=".5" stop-color="#ffa930"/><stop offset=".73" stop-color="#ff7e37"/><stop offset=".79" stop-color="#ff7139"/></radialGradient><radialGradient id="h" cx="373.96" cy="-74.29" r="732.21" gradientUnits="userSpaceOnUse"><stop offset=".11" stop-color="#fff44f"/><stop offset=".46" stop-color="#ff980e"/><stop offset=".62" stop-color="#ff5634"/><stop offset=".72" stop-color="#ff3647"/><stop offset=".9" stop-color="#e31587"/></radialGradient><radialGradient id="i" cx="304.59" cy="7.07" r="536.37" gradientTransform="matrix(.1 .99 -.65 .07 277.32 -296.33)" gradientUnits="userSpaceOnUse"><stop offset="0" stop-color="#fff44f"/><stop offset=".06" stop-color="#ffe847"/><stop offset=".17" stop-color="#ffc830"/><stop offset=".3" stop-color="#ff980e"/><stop offset=".36" stop-color="#ff8b16"/><stop offset=".45" stop-color="#ff672a"/><stop offset=".57" stop-color="#ff3647"/><stop offset=".74" stop-color="#e31587"/></radialGradient><radialGradient id="j" cx="235.02" cy="98.12" r="457.12" gradientUnits="userSpaceOnUse"><stop offset=".14" stop-color="#fff44f"/><stop offset=".48" stop-color="#ff980e"/><stop offset=".59" stop-color="#ff5634"/><stop offset=".66" stop-color="#ff3647"/><stop offset=".9" stop-color="#e31587"/></radialGradient><radialGradient id="k" cx="355.69" cy="124.88" r="500.32" gradientUnits="userSpaceOnUse"><stop offset=".09" stop-color="#fff44f"/><stop offset=".23" stop-color="#ffe141"/><stop offset=".51" stop-color="#ffaf1e"/><stop offset=".63" stop-color="#ff980e"/></radialGradient><linearGradient id="l" x1="442.1" y1="74.79" x2="102.64" y2="414.26" gradientUnits="userSpaceOnUse"><stop offset=".17" stop-color="#fff44f" stop-opacity=".8"/><stop offset=".27" stop-color="#fff44f" stop-opacity=".63"/><stop offset=".49" stop-color="#fff44f" stop-opacity=".22"/><stop offset=".6" stop-color="#fff44f" stop-opacity="0"/></linearGradient></defs><path d="M478.71 166.38c-10.44-25.13-31.6-52.26-48.21-60.83 13.52 26.51 21.34 53.1 24.33 72.94 0 0 0 .14.05.4C427.71 111.15 381.63 83.83 344 24.36c-1.91-3-3.81-6-5.67-9.21-1.05-1.81-1.9-3.45-2.64-5A43.73 43.73 0 0 1 332.1.68a.63.63 0 0 0-.54-.65.86.86 0 0 0-.45 0 .47.47 0 0 0-.11.07c-.06 0-.12.07-.18.09l.1-.13c-60.37 35.36-80.85 100.77-82.73 133.5A120.27 120.27 0 0 0 182 159.05a71.11 71.11 0 0 0-6.22-4.7 111.39 111.39 0 0 1-.67-58.74c-24.69 11.25-43.89 29-57.85 44.71h-.11c-9.53-12.07-8.86-51.88-8.32-60.19-.11-.52-7.1 3.63-8 4.26a174.74 174.74 0 0 0-23.49 20.12 210.18 210.18 0 0 0-22.44 26.91 202.86 202.86 0 0 0-32.25 72.82c-.11.52-.21 1.06-.32 1.59-.45 2.11-2.08 12.7-2.37 15v.53A229.1 229.1 0 0 0 16 254.52v1.23c0 132.72 107.6 240.32 240.32 240.32 118.87 0 217.56-86.3 236.89-199.66.4-3.07.73-6.17 1.09-9.27 4.78-41.22-.53-84.54-15.59-120.76zm-277 188.12c1.12.53 2.18 1.12 3.33 1.64l.16.1c-1.15-.56-2.32-1.14-3.48-1.74zm55-144.95zM454.9 179v-.23.26z" fill="url(#a)"/><path d="M478.71 166.38c-10.44-25.13-31.6-52.26-48.21-60.83 13.52 26.51 21.34 53.1 24.33 72.94v.49c22.68 61.48 10.32 124-7.48 162.2-27.54 59.1-94.21 119.68-198.58 116.73-112.67-3.2-212-86.91-230.55-196.46-3.38-17.29 0-26.06 1.7-40.09-2.07 10.81-2.86 13.94-3.89 33.16v1.23c0 132.72 107.6 240.32 240.32 240.32 118.87 0 217.56-86.3 236.89-199.66.4-3.07.73-6.17 1.09-9.27 4.75-41.22-.56-84.54-15.62-120.76z" fill="url(#b)"/><path d="M478.71 166.38c-10.44-25.13-31.6-52.26-48.21-60.83 13.52 26.51 21.34 53.1 24.33 72.94v.49c22.68 61.48 10.32 124-7.48 162.2-27.54 59.1-94.21 119.68-198.58 116.73-112.67-3.2-212-86.91-230.55-196.46-3.38-17.29 0-26.06 1.7-40.09-2.07 10.81-2.86 13.94-3.89 33.16v1.23c0 132.72 107.6 240.32 240.32 240.32 118.87 0 217.56-86.3 236.89-199.66.4-3.07.73-6.17 1.09-9.27 4.75-41.22-.56-84.54-15.62-120.76z" fill="url(#c)"/><path d="M361.92 194.62c.53.37 1 .74 1.5 1.11a130.49 130.49 0 0 0-22.32-29.12C266.4 91.91 321.52 4.63 330.81.19l.1-.13c-60.37 35.36-80.85 100.77-82.73 133.5 2.8-.19 5.59-.43 8.44-.43 45.05 0 84.29 24.78 105.3 61.49z" fill="url(#d)"/><path d="M256.77 209.54c-.39 6-21.51 26.6-28.9 26.6-68.34 0-79.43 41.34-79.43 41.34 3 34.82 27.26 63.49 56.61 78.66 1.34.69 2.69 1.31 4 1.93 2.35 1 4.71 2 7.06 2.89a106.88 106.88 0 0 0 31.27 6c119.78 5.62 143-143.22 56.55-186.44 22.13-3.85 45.11 5.06 57.94 14.07-21-36.71-60.25-61.49-105.3-61.49-2.85 0-5.64.24-8.44.43A120.27 120.27 0 0 0 182 159.05c3.67 3.11 7.81 7.25 16.52 15.83 16.34 16.12 58.16 32.71 58.25 34.66z" fill="url(#e)"/><path d="M256.77 209.54c-.39 6-21.51 26.6-28.9 26.6-68.34 0-79.43 41.34-79.43 41.34 3 34.82 27.26 63.49 56.61 78.66 1.34.69 2.69 1.31 4 1.93 2.35 1 4.71 2 7.06 2.89a106.88 106.88 0 0 0 31.27 6c119.78 5.62 143-143.22 56.55-186.44 22.13-3.85 45.11 5.06 57.94 14.07-21-36.71-60.25-61.49-105.3-61.49-2.85 0-5.64.24-8.44.43A120.27 120.27 0 0 0 182 159.05c3.67 3.11 7.81 7.25 16.52 15.83 16.34 16.12 58.16 32.71 58.25 34.66z" fill="url(#f)"/><path d="M170.83 151.06c1.95 1.26 3.55 2.32 5 3.29a111.39 111.39 0 0 1-.67-58.74c-24.69 11.25-43.89 29-57.85 44.71 1.13-.03 35.99-.66 53.52 10.74z" fill="url(#g)"/><path d="M18.22 261.45C36.77 371 136.1 454.71 248.85 457.91c104.37 2.95 171-57.63 198.58-116.73 17.8-38.21 30.16-100.72 7.48-162.2v-.46c0 .06 0 .14.05.4 8.52 55.67-19.79 109.6-64.05 146.07a7.3 7.3 0 0 0-.13.31c-86.25 70.23-168.78 42.37-185.49 31-1.16-.56-2.33-1.14-3.49-1.74-50.29-24-71.06-69.85-66.61-109.14-42.45 0-56.93-35.82-56.93-35.82s38.12-27.18 88.36-3.54c46.53 21.9 90.23 3.55 90.23 3.54-.09-1.95-41.91-18.59-58.22-34.66-8.71-8.58-12.85-12.72-16.52-15.83a71.11 71.11 0 0 0-6.22-4.7c-1.43-1-3-2-5-3.29-17.53-11.4-52.39-10.77-53.54-10.74h-.11c-9.53-12.07-8.86-51.88-8.32-60.19-.11-.52-7.1 3.63-8 4.26a174.74 174.74 0 0 0-23.49 20.12 210.18 210.18 0 0 0-22.44 26.91 202.86 202.86 0 0 0-32.25 72.82c-.19.46-8.74 37.79-4.52 57.15z" fill="url(#h)"/><path d="M341.1 166.61a130.49 130.49 0 0 1 22.32 29.12 46.36 46.36 0 0 1 3.6 3C421.5 248.89 393 319.9 390.83 325c44.26-36.47 72.57-90.4 64.05-146.07-27.17-67.78-73.25-95.1-110.88-154.57-1.91-3-3.81-6-5.67-9.21-1.05-1.81-1.9-3.45-2.64-5A43.73 43.73 0 0 1 332.1.68a.63.63 0 0 0-.54-.65.86.86 0 0 0-.45 0 .47.47 0 0 0-.11.07c-.06 0-.12.07-.18.09-9.3 4.44-64.42 91.72 10.28 166.42z" fill="url(#i)"/><path d="M367 198.68a46.36 46.36 0 0 0-3.6-3c-.49-.37-1-.74-1.5-1.11-12.83-9-35.81-17.92-57.94-14.07 86.44 43.22 63.23 192.06-56.55 186.44a106.88 106.88 0 0 1-31.27-6c-2.35-.88-4.71-1.85-7.06-2.89-1.36-.62-2.71-1.24-4-1.93l.16.1c16.71 11.4 99.24 39.26 185.49-31 0 0 .05-.13.13-.31C393 319.9 421.5 248.89 367 198.68z" fill="url(#j)"/><path d="M148.44 277.48s11.09-41.34 79.43-41.34c7.39 0 28.51-20.62 28.9-26.6s-43.7 18.36-90.23-3.54c-50.24-23.64-88.36 3.54-88.36 3.54s14.48 35.82 56.93 35.82c-4.45 39.29 16.32 85.1 66.61 109.14 1.12.53 2.18 1.12 3.33 1.64-29.35-15.14-53.58-43.84-56.61-78.66z" fill="url(#k)"/><path d="M478.71 166.38c-10.44-25.13-31.6-52.26-48.21-60.83 13.52 26.51 21.34 53.1 24.33 72.94 0 0 0 .14.05.4C427.71 111.15 381.63 83.83 344 24.36c-1.91-3-3.81-6-5.67-9.21-1.05-1.81-1.9-3.45-2.64-5A43.73 43.73 0 0 1 332.1.68a.63.63 0 0 0-.54-.65.86.86 0 0 0-.45 0 .47.47 0 0 0-.11.07c-.06 0-.12.07-.18.09l.1-.13c-60.37 35.36-80.85 100.77-82.73 133.5 2.8-.19 5.59-.43 8.44-.43 45.05 0 84.29 24.78 105.3 61.49-12.83-9-35.81-17.92-57.94-14.07 86.44 43.22 63.23 192.06-56.55 186.44a106.88 106.88 0 0 1-31.27-6c-2.35-.88-4.71-1.85-7.06-2.89-1.36-.62-2.71-1.24-4-1.93l.16.1c-1.16-.56-2.33-1.14-3.49-1.74 1.12.53 2.18 1.12 3.33 1.64-29.41-15.17-53.64-43.87-56.67-78.69 0 0 11.09-41.34 79.43-41.34 7.39 0 28.51-20.62 28.9-26.6-.09-1.95-41.91-18.59-58.22-34.66-8.71-8.58-12.85-12.72-16.52-15.83a71.11 71.11 0 0 0-6.22-4.7 111.39 111.39 0 0 1-.67-58.74c-24.69 11.25-43.89 29-57.85 44.71h-.11c-9.53-12.07-8.86-51.88-8.32-60.19-.11-.52-7.1 3.63-8 4.26a174.74 174.74 0 0 0-23.49 20.12 210.18 210.18 0 0 0-22.44 26.91 202.86 202.86 0 0 0-32.25 72.82c-.11.52-.21 1.06-.32 1.59-.45 2.11-2.49 12.85-2.77 15.15 0 .18 0-.17 0 0A278.84 278.84 0 0 0 16 254.52v1.23c0 132.72 107.6 240.32 240.32 240.32 118.87 0 217.56-86.3 236.89-199.66.4-3.07.73-6.17 1.09-9.27 4.78-41.22-.53-84.54-15.59-120.76zm-23.84 12.34v.26z" fill="url(#l)"/></svg>`,
		flexDirection: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M80-160v-280h360v280H80Zm0-360v-280h360v280H80Zm80-80h200v-120H160v120Zm560 440L520-360l56-56 104 103v-487h80v487l104-103 56 56-200 200Z"/></svg>`,
		flexDirectionAlt: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"><path d="M20 2L13 2L13 11H20V2ZM11 2L4 2L4 11L11 11V2ZM9 4V9H6L6 4L9 4ZM20 18L15 13L13.6 14.4L16.175 17H4L4 19H16.175L13.6 21.6L15 23L20 18Z"/></svg>`,
		flow: Q`<svg viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg"> <path d="M29.2001 16.5926C23.9293 20.6091 15.0548 17.6089 11.896 13.4654C7.12556 7.21823 -1.93095 14.6559 3.88687 20.9269C11.4117 28.7768 24.8103 26.1018 29.2001 16.5926Z" fill="#00B4F0"></path> <path d="M3.88117 20.9347C11.4137 28.7768 24.8201 26.1018 29.2016 16.6004C33.5277 6.988 18.994 1.98738 16.1919 11.7828C15.806 13.1957 15.1718 14.5286 14.319 15.7194C12.0012 18.9498 8.19112 20.9347 3.88117 20.9347Z" fill="#FF707A"></path> <path d="M29.2016 16.5926C24.9632 19.8313 18.3749 18.5137 14.319 15.7116C12.0012 18.9503 8.19112 20.9347 3.88117 20.9264C6.93682 24.1096 10.9694 25.562 14.9698 25.5226C20.812 25.4749 26.5981 22.2445 29.2016 16.5926Z" fill="#5748FF"></path></svg>`,
		formatColorText: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M80 0v-160h800V0H80Zm140-280 210-560h100l210 560h-96l-50-144H368l-52 144h-96Zm176-224h168l-82-232h-4l-82 232Z"/></svg>`,
		formatImageLeft: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M120-280v-400h400v400H120Zm80-80h240v-240H200v240Zm-80-400v-80h720v80H120Zm480 160v-80h240v80H600Zm0 160v-80h240v80H600Zm0 160v-80h240v80H600ZM120-120v-80h720v80H120Z"/></svg>`,
		forum: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M880-80 720-240H320q-33 0-56.5-23.5T240-320v-40h440q33 0 56.5-23.5T760-440v-280h40q33 0 56.5 23.5T880-640v560ZM160-473l47-47h393v-280H160v327ZM80-280v-520q0-33 23.5-56.5T160-880h440q33 0 56.5 23.5T680-800v280q0 33-23.5 56.5T600-440H240L80-280Zm80-240v-280 280Z"/></svg>`,
		github: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"><path d="M10.226 17.284c-2.965-.36-5.054-2.493-5.054-5.256 0-1.123.404-2.336 1.078-3.144-.292-.741-.247-2.314.09-2.965.898-.112 2.111.36 2.83 1.01.853-.269 1.752-.404 2.853-.404 1.1 0 1.999.135 2.807.382.696-.629 1.932-1.1 2.83-.988.315.606.36 2.179.067 2.942.72.854 1.101 2 1.101 3.167 0 2.763-2.089 4.852-5.098 5.234.763.494 1.28 1.572 1.28 2.807v2.336c0 .674.561 1.056 1.235.786 4.066-1.55 7.255-5.615 7.255-10.646C23.5 6.188 18.334 1 11.978 1 5.62 1 .5 6.188.5 12.545c0 4.986 3.167 9.12 7.435 10.669.606.225 1.19-.18 1.19-.786V20.63a2.9 2.9 0 0 1-1.078.224c-1.483 0-2.359-.808-2.987-2.313-.247-.607-.517-.966-1.034-1.033-.27-.023-.359-.135-.359-.27 0-.27.45-.471.898-.471.652 0 1.213.404 1.797 1.235.45.651.921.943 1.483.943.561 0 .92-.202 1.437-.719.382-.381.674-.718.944-.943"></path></svg>`,
		globe: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-7-.5-14.5T799-507q-5 29-27 48t-52 19h-80q-33 0-56.5-23.5T560-520v-40H400v-80q0-33 23.5-56.5T480-720h40q0-23 12.5-40.5T563-789q-20-5-40.5-8t-42.5-3q-134 0-227 93t-93 227h200q66 0 113 47t47 113v40H400v110q20 5 39.5 7.5T480-160Z"/></svg>`,
		gridView: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M120-520v-320h320v320H120Zm0 400v-320h320v320H120Zm400-400v-320h320v320H520Zm0 400v-320h320v320H520ZM200-600h160v-160H200v160Zm400 0h160v-160H600v160Zm0 400h160v-160H600v160Zm-400 0h160v-160H200v160Zm400-400Zm0 240Zm-240 0Zm0-240Z"/></svg>`,
		group: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M40-160v-112q0-34 17.5-62.5T104-378q62-31 126-46.5T360-440q66 0 130 15.5T616-378q29 15 46.5 43.5T680-272v112H40Zm720 0v-120q0-44-24.5-84.5T666-434q51 6 96 20.5t84 35.5q36 20 55 44.5t19 53.5v120H760ZM360-480q-66 0-113-47t-47-113q0-66 47-113t113-47q66 0 113 47t47 113q0 66-47 113t-113 47Zm400-160q0 66-47 113t-113 47q-11 0-28-2.5t-28-5.5q27-32 41.5-71t14.5-81q0-42-14.5-81T544-792q14-5 28-6.5t28-1.5q66 0 113 47t47 113ZM120-240h480v-32q0-11-5.5-20T580-306q-54-27-109-40.5T360-360q-56 0-111 13.5T140-306q-9 5-14.5 14t-5.5 20v32Zm240-320q33 0 56.5-23.5T440-640q0-33-23.5-56.5T360-720q-33 0-56.5 23.5T280-640q0 33 23.5 56.5T360-560Zm0 320Zm0-400Z"/></svg>`,
		h1: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M200-280v-400h80v160h160v-160h80v400h-80v-160H280v160h-80Zm480 0v-320h-80v-80h160v400h-80Z"/></svg>`,
		h2: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M120-280v-400h80v160h160v-160h80v400h-80v-160H200v160h-80Zm400 0v-160q0-33 23.5-56.5T600-520h160v-80H520v-80h240q33 0 56.5 23.5T840-600v80q0 33-23.5 56.5T760-440H600v80h240v80H520Z"/></svg>`,
		h3: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M120-280v-400h80v160h160v-160h80v400h-80v-160H200v160h-80Zm400 0v-80h240v-80H600v-80h160v-80H520v-80h240q33 0 56.5 23.5T840-600v240q0 33-23.5 56.5T760-280H520Z"/></svg>`,
		h4: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M120-280v-400h80v160h160v-160h80v400h-80v-160H200v160h-80Zm600 0v-120H520v-280h80v200h120v-200h80v200h80v80h-80v120h-80Z"/></svg>`,
		h5: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M120-280v-400h80v160h160v-160h80v400h-80v-160H200v160h-80Zm400 0v-80h240v-80H520v-240h320v80H600v80h160q33 0 56.5 23.5T840-440v80q0 33-23.5 56.5T760-280H520Z"/></svg>`,
		h6: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M120-280v-400h80v160h160v-160h80v400h-80v-160H200v160h-80Zm480 0q-33 0-56.5-23.5T520-360v-240q0-33 23.5-56.5T600-680h240v80H600v80h160q33 0 56.5 23.5T840-440v80q0 33-23.5 56.5T760-280H600Zm0-160v80h160v-80H600Z"/></svg>`,
		handyman: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M754-81q-8 0-15-2.5T726-92L522-296q-6-6-8.5-13t-2.5-15q0-8 2.5-15t8.5-13l85-85q6-6 13-8.5t15-2.5q8 0 15 2.5t13 8.5l204 204q6 6 8.5 13t2.5 15q0 8-2.5 15t-8.5 13l-85 85q-6 6-13 8.5T754-81Zm0-95 29-29-147-147-29 29 147 147ZM205-80q-8 0-15.5-3T176-92l-84-84q-6-6-9-13.5T80-205q0-8 3-15t9-13l212-212h85l34-34-165-165h-57L80-765l113-113 121 121v57l165 165 116-116-43-43 56-56H495l-28-28 142-142 28 28v113l56-56 142 142q17 17 26 38.5t9 45.5q0 24-9 46t-26 39l-85-85-56 56-42-42-207 207v84L233-92q-6 6-13 9t-15 3Zm0-96 170-170v-29h-29L176-205l29 29Zm0 0-29-29 15 14 14 15Zm549 0 29-29-29 29Z"/></svg>`,
		height: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-120 320-280l56-56 64 63v-414l-64 63-56-56 160-160 160 160-56 57-64-64v414l64-63 56 56-160 160Z"/></svg>`,
		help: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M478-240q21 0 35.5-14.5T528-290q0-21-14.5-35.5T478-340q-21 0-35.5 14.5T428-290q0 21 14.5 35.5T478-240Zm-36-154h74q0-33 7.5-52t42.5-52q26-26 41-49.5t15-56.5q0-56-41-86t-97-30q-57 0-92.5 30T342-618l66 26q5-18 22.5-39t53.5-21q32 0 48 17.5t16 38.5q0 20-12 37.5T506-526q-44 39-54 59t-10 73Zm38 314q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-320Z"/></svg>`,
		hilla: Q`<svg viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg"><g clip-path="url(#clip0_119_352)"><mask id="mask0_119_352" style="mask-type:alpha" maskUnits="userSpaceOnUse" x="2" y="3" width="28" height="26"><path d="M29.8034 14.585C29.2782 12.5054 27.4472 10.8744 25.3446 10.5712C27.4916 4.6308 19.9642 0.321533 15.9837 5.14358C11.9971 0.318098 4.47153 4.63852 6.62451 10.5737C0.461059 11.6526 0.455935 20.3656 6.62451 21.4435C5.4212 24.4387 7.18133 27.9542 10.2968 28.7564C12.3345 29.3415 14.6847 28.5555 15.9811 26.878C16.9547 28.1501 18.6688 28.9712 20.2418 28.9395C24.0055 28.9884 26.7793 25.0296 25.3403 21.4367C28.4686 21.021 30.6899 17.7089 29.8034 14.585ZM17.5687 18.6563C17.5294 18.6804 17.4893 18.7035 17.4492 18.7258C16.5909 19.2197 15.3799 19.2197 14.5216 18.7258C14.4823 18.7018 14.4422 18.6786 14.402 18.6563H14.3944C13.9551 18.3883 13.5894 18.0141 13.3308 17.5676C13.0721 17.1211 12.9287 16.6166 12.9135 16.1001C12.9135 16.04 12.9135 15.9799 12.9135 15.9206C12.9373 15.1165 13.2717 14.3534 13.8457 13.7933C14.4197 13.2332 15.1882 12.92 15.988 12.9203C19.1094 12.9521 20.2043 17.0432 17.5807 18.6563H17.5687ZM11.7119 5.4777C12.2095 5.47805 12.6995 5.59984 13.14 5.83263C13.5804 6.06542 13.9581 6.40226 14.2407 6.81421C14.4374 7.09389 14.6986 7.32153 15.0018 7.47767C15.305 7.63383 15.6413 7.71385 15.982 7.71092C16.3289 7.71077 16.6706 7.62598 16.9778 7.4638C17.2849 7.30164 17.5484 7.06694 17.7455 6.77985C19.3203 4.48908 23.2505 5.35402 23.3282 8.56986C23.3281 9.0984 23.1932 9.61807 22.9364 10.0793C22.6798 10.5404 22.3098 10.9278 21.8619 11.2042C20.4485 12.0992 18.6166 11.607 17.7284 10.3307C17.5309 10.0501 17.2691 9.82161 16.9651 9.6646C16.661 9.50759 16.3238 9.4267 15.982 9.4288C15.6426 9.4259 15.3076 9.50583 15.0056 9.66171C14.7037 9.81759 14.4439 10.0448 14.2483 10.3238C13.2038 11.8493 10.8809 12.1834 9.49572 10.7112C7.63226 8.7846 9.01406 5.46997 11.7119 5.4777ZM14.6634 24.3055C14.0912 26.3729 11.5428 27.231 9.83903 25.9014C8.66048 25.0089 8.33256 23.3744 8.94147 22.0989C9.08238 21.8003 9.14893 21.4717 9.13539 21.1414C9.12186 20.8112 9.02863 20.4892 8.86375 20.2032L8.8031 20.0985C8.63594 19.8128 8.40255 19.572 8.12282 19.3967C7.84309 19.2214 7.52534 19.1168 7.1967 19.0918C6.21116 19.0059 5.26747 18.4382 4.77897 17.5543C3.42279 15.2068 5.58858 12.3449 8.17797 13.0079C10.1422 13.4864 11.0159 15.6707 10.2165 17.349C10.0745 17.656 10.0089 17.9932 10.0254 18.3314C10.042 18.6695 10.1401 18.9986 10.3113 19.2902L10.3292 19.3211C10.499 19.6155 10.737 19.8642 11.0229 20.046C11.3089 20.2279 11.6343 20.3375 11.9715 20.3656C12.4271 20.4045 12.8685 20.5448 13.2637 20.7762C14.4601 21.4478 15.0682 22.9965 14.6651 24.3055H14.6634ZM27.185 17.5543C26.9356 17.9886 26.5847 18.3553 26.163 18.6229C25.7412 18.8904 25.261 19.0507 24.7639 19.09C24.439 19.1121 24.1244 19.2135 23.8472 19.3853C23.57 19.5571 23.3385 19.7942 23.1728 20.0761L23.0934 20.2144C22.9276 20.4976 22.834 20.8175 22.8208 21.1459C22.8077 21.4742 22.8755 21.8007 23.0183 22.0963C24.2771 24.625 21.5468 27.6794 18.7149 26.1247C16.6712 24.9437 16.6823 21.9503 18.7003 20.7762C19.1047 20.5401 19.5569 20.3991 20.0232 20.3638C20.351 20.3378 20.6676 20.2318 20.9455 20.0551C21.2235 19.8783 21.4545 19.6361 21.6185 19.3495L21.6851 19.2335C21.847 18.9448 21.937 18.6208 21.9471 18.2894C21.9572 17.9581 21.8873 17.6292 21.7432 17.331C21.095 15.9421 21.5561 14.1632 22.9568 13.3447C23.2156 13.1915 23.4952 13.0772 23.7869 13.0054C26.4104 12.3569 28.5361 15.1974 27.1867 17.5543H27.185Z" fill="url(#paint0_linear_119_352)"></path></mask><g mask="url(#mask0_119_352)"><path d="M-36.1968 8.19483L29.877 -31.3765L47.2498 -2.36846L-18.8241 37.2029L-36.1968 8.19483Z" fill="#FF707A"></path><path d="M-36.1968 8.19483L29.877 -31.3765L47.2498 -2.36846L-18.8241 37.2029L-36.1968 8.19483Z" fill="#FF707A"></path><path d="M-21.7942 38.8772L44.2796 -0.694092L61.6524 28.3139L-4.42145 67.8853L-21.7942 38.8772Z" fill="#FF707A"></path><path d="M-21.7942 38.8772L44.2796 -0.694092L61.6524 28.3139L-4.42145 67.8853L-21.7942 38.8772Z" fill="#00B4F0"></path></g></g><defs><linearGradient id="paint0_linear_119_352" x1="32.5251" y1="22.9471" x2="-6.14001" y2="-0.533752" gradientUnits="userSpaceOnUse"><stop stop-color="#FF707A"></stop><stop offset="0.467045" stop-color="#5748FF"></stop><stop offset="1" stop-color="#00B4F0"></stop></linearGradient><clipPath id="clip0_119_352"><rect width="28" height="26" fill="white" transform="translate(2 3)"></rect></clipPath></defs></svg>`,
		history: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-120q-138 0-240.5-91.5T122-440h82q14 104 92.5 172T480-200q117 0 198.5-81.5T760-480q0-117-81.5-198.5T480-760q-69 0-129 32t-101 88h110v80H120v-240h80v94q51-64 124.5-99T480-840q75 0 140.5 28.5t114 77q48.5 48.5 77 114T840-480q0 75-28.5 140.5t-77 114q-48.5 48.5-114 77T480-120Zm112-192L440-464v-216h80v184l128 128-56 56Z"/></svg>`,
		historyToggleOff: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M612-292 440-464v-216h80v184l148 148-56 56Zm-498-25q-13-29-21-60t-11-63h81q3 21 8.5 42t13.5 41l-71 40ZM82-520q3-32 11-63.5t22-60.5l70 40q-8 20-13.5 41t-8.5 43H82Zm165 366q-27-20-50-43.5T154-248l70-40q14 18 29.5 33.5T287-225l-40 71Zm-22-519-71-40q20-27 43-50t50-43l40 71q-17 14-32.5 29.5T225-673ZM440-82q-32-3-63.5-11T316-115l40-70q20 8 41 13.5t43 8.5v81Zm-84-693-40-70q29-14 60.5-22t63.5-11v81q-22 3-43 8.5T356-775ZM520-82v-81q22-3 43-8.5t41-13.5l40 70q-29 14-60.5 22T520-82Zm84-693q-20-8-41-13.5t-43-8.5v-81q32 3 63.5 11t60.5 22l-40 70Zm109 621-40-71q17-14 32.5-29.5T735-287l71 40q-20 27-43 50.5T713-154Zm22-519q-14-17-29.5-32.5T673-735l40-71q27 19 50 42t42 50l-70 41Zm62 153q-3-22-8.5-43T775-604l70-41q13 30 21.5 61.5T878-520h-81Zm48 204-70-40q8-20 13.5-41t8.5-43h81q-3 32-11 63.5T845-316Z"/></svg>`,
		home: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M240-200h120v-240h240v240h120v-360L480-740 240-560v360Zm-80 80v-480l320-240 320 240v480H520v-240h-80v240H160Zm320-350Z"/></svg>`,
		horizontalRule: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M160-440v-80h640v80H160Z"/></svg>`,
		idCard: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M560-440h200v-80H560v80Zm0-120h200v-80H560v80ZM200-320h320v-22q0-45-44-71.5T360-440q-72 0-116 26.5T200-342v22Zm160-160q33 0 56.5-23.5T440-560q0-33-23.5-56.5T360-640q-33 0-56.5 23.5T280-560q0 33 23.5 56.5T360-480ZM160-160q-33 0-56.5-23.5T80-240v-480q0-33 23.5-56.5T160-800h640q33 0 56.5 23.5T880-720v480q0 33-23.5 56.5T800-160H160Zm0-80h640v-480H160v480Zm0 0v-480 480Z"/></svg>`,
		image: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-560H200v560Zm40-80h480L570-480 450-320l-90-120-120 160Zm-40 80v-560 560Z"/></svg>`,
		importContacts: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M260-320q47 0 91.5 10.5T440-278v-394q-41-24-87-36t-93-12q-36 0-71.5 7T120-692v396q35-12 69.5-18t70.5-6Zm260 42q44-21 88.5-31.5T700-320q36 0 70.5 6t69.5 18v-396q-33-14-68.5-21t-71.5-7q-47 0-93 12t-87 36v394Zm-40 118q-48-38-104-59t-116-21q-42 0-82.5 11T100-198q-21 11-40.5-1T40-234v-482q0-11 5.5-21T62-752q46-24 96-36t102-12q58 0 113.5 15T480-740q51-30 106.5-45T700-800q52 0 102 12t96 36q11 5 16.5 15t5.5 21v482q0 23-19.5 35t-40.5 1q-37-20-77.5-31T700-240q-60 0-116 21t-104 59ZM280-494Z"/></svg>`,
		inbox: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-120H640q-30 38-71.5 59T480-240q-47 0-88.5-21T320-320H200v120Zm280-120q38 0 69-22t43-58h168v-360H200v360h168q12 36 43 58t69 22ZM200-200h560-560Z"/></svg>`,
		info: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M440-280h80v-240h-80v240Zm40-320q17 0 28.5-11.5T520-640q0-17-11.5-28.5T480-680q-17 0-28.5 11.5T440-640q0 17 11.5 28.5T480-600Zm0 520q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-320Z"/></svg>`,
		inkSelection: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M440-120v-400h400v80H576l264 264-56 56-264-264v264h-80Zm-160 0v-80h80v80h-80Zm-80-640h-80q0-33 23.5-56.5T200-840v80Zm80 0v-80h80v80h-80Zm160 0v-80h80v80h-80Zm160 0v-80h80v80h-80Zm160 0v-80q33 0 56.5 23.5T840-760h-80ZM200-200v80q-33 0-56.5-23.5T120-200h80Zm-80-80v-80h80v80h-80Zm0-160v-80h80v80h-80Zm0-160v-80h80v80h-80Zm640 0v-80h80v80h-80Z"/></svg>`,
		input: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M160-160q-33 0-56.5-23.5T80-240v-120h80v120h640v-480H160v120H80v-120q0-33 23.5-56.5T160-800h640q33 0 56.5 23.5T880-720v480q0 33-23.5 56.5T800-160H160Zm300-140-56-58 83-82H80v-80h407l-83-82 56-58 180 180-180 180Z"/></svg>`,
		intelliJ: Q`<svg fill="none" viewBox="0 0 64 64"><defs><linearGradient id="__JETBRAINS_COM__LOGO_PREFIX__2" x1="-0.391" x2="24.392" y1="7.671" y2="61.126" gradientUnits="userSpaceOnUse"><stop offset="0.1" stop-color="#FC801D"></stop><stop offset="0.59" stop-color="#FE2857"></stop></linearGradient><linearGradient id="__JETBRAINS_COM__LOGO_PREFIX__1" x1="4.325" x2="62.921" y1="59.932" y2="1.336" gradientUnits="userSpaceOnUse"><stop offset="0.21" stop-color="#FE2857"></stop><stop offset="0.7" stop-color="#007EFF"></stop></linearGradient></defs><path fill="#FF8100" d="M16.45 6H4.191a4.125 4.125 0 0 0-4.124 4.19l.176 11.044a4.125 4.125 0 0 0 1.44 3.066l38.159 32.707c.747.64 1.7.993 2.684.993h11.35A4.125 4.125 0 0 0 58 53.875V42.872c0-1.19-.514-2.321-1.41-3.105L19.167 7.021A4.123 4.123 0 0 0 16.45 6Z"></path><path fill="url(#__JETBRAINS_COM__LOGO_PREFIX__2)" d="M14.988 6H4.125A4.125 4.125 0 0 0 0 10.125v12.566c0 .2.014.4.044.598l5.448 37.185A4.125 4.125 0 0 0 9.573 64h15.398a4.125 4.125 0 0 0 4.125-4.127L29.09 41.37c0-.426-.066-.849-.195-1.254l-9.98-31.245A4.126 4.126 0 0 0 14.988 6Z"></path><path fill="url(#__JETBRAINS_COM__LOGO_PREFIX__1)" d="M59.876 0H25.748a4.125 4.125 0 0 0-3.8 2.52L6.151 39.943a4.118 4.118 0 0 0-.325 1.638l.15 18.329A4.125 4.125 0 0 0 10.101 64h17.666c.806 0 1.593-.236 2.266-.678l32.11-21.109A4.123 4.123 0 0 0 64 38.766V4.125A4.125 4.125 0 0 0 59.876 0Z"></path><path fill="#000" d="M52 12H12v40h40V12Z"></path><path fill="#fff" d="M33 44H17v3h16v-3ZM17 29.383h2.98v-9.775H17v-2.616h8.843v2.616h-2.98v9.775h2.98V32H17v-2.616Zm10.643-.085h2.154a2.38 2.38 0 0 0 1.163-.279c.34-.186.602-.448.788-.788.186-.34.279-.727.279-1.163V16.992h2.926v10.28c0 .9-.207 1.709-.622 2.427a4.45 4.45 0 0 1-1.715 1.688c-.728.408-1.546.611-2.454.611h-2.519v-2.7Z"></path></svg>`,
		java: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 300 400"><path fill="#5382A1" d="M102.681,291.324c0,0-14.178,8.245,10.09,11.035c29.4,3.354,44.426,2.873,76.825-3.259c0,0,8.518,5.341,20.414,9.967C137.38,340.195,45.634,307.264,102.681,291.324"/><path fill="#5382A1" d="M93.806,250.704c0,0-15.902,11.771,8.384,14.283c31.406,3.24,56.208,3.505,99.125-4.759c0,0,5.936,6.018,15.27,9.309C128.771,295.215,30.962,271.562,93.806,250.704"/><path fill="#F8981D" d="M168.625,181.799c17.896,20.604-4.702,39.145-4.702,39.145s45.441-23.458,24.572-52.833c-19.491-27.394-34.438-41.005,46.479-87.934C234.974,80.177,107.961,111.899,168.625,181.799"/><path fill="#5382A1" d="M264.684,321.369c0,0,10.492,8.645-11.555,15.333c-41.923,12.7-174.488,16.535-211.314,0.506c-13.238-5.759,11.587-13.751,19.396-15.428c8.144-1.766,12.798-1.437,12.798-1.437c-14.722-10.371-95.157,20.364-40.857,29.166C181.236,373.524,303.095,338.695,264.684,321.369"/><path fill="#5382A1" d="M109.499,208.617c0,0-67.431,16.016-23.879,21.832c18.389,2.462,55.047,1.905,89.193-0.956c27.906-2.354,55.927-7.359,55.927-7.359s-9.84,4.214-16.959,9.075c-68.475,18.009-200.756,9.631-162.674-8.79C83.313,206.851,109.499,208.617,109.499,208.617"/><path fill="#5382A1" d="M230.462,276.231c69.608-36.171,37.424-70.931,14.96-66.248c-5.506,1.146-7.961,2.139-7.961,2.139s2.044-3.202,5.948-4.588c44.441-15.624,78.619,46.081-14.346,70.52C229.063,278.055,230.14,277.092,230.462,276.231"/><path fill="#F8981D" d="M188.495,4.399c0,0,38.55,38.563-36.563,97.862c-60.233,47.568-13.735,74.69-0.025,105.678c-35.159-31.722-60.961-59.647-43.651-85.637C133.663,84.151,204.049,65.654,188.495,4.399"/><path fill="#5382A1" d="M116.339,374.246c66.815,4.277,169.417-2.373,171.847-33.988c0,0-4.671,11.985-55.219,21.503c-57.028,10.732-127.364,9.479-169.081,2.601C63.887,364.361,72.426,371.43,116.339,374.246"/></svg>`,
		key: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M280-400q-33 0-56.5-23.5T200-480q0-33 23.5-56.5T280-560q33 0 56.5 23.5T360-480q0 33-23.5 56.5T280-400Zm0 160q-100 0-170-70T40-480q0-100 70-170t170-70q67 0 121.5 33t86.5 87h352l120 120-180 180-80-60-80 60-85-60h-47q-32 54-86.5 87T280-240Zm0-80q56 0 98.5-34t56.5-86h125l58 41 82-61 71 55 75-75-40-40H435q-14-52-56.5-86T280-640q-66 0-113 47t-47 113q0 66 47 113t113 47Z"/></svg>`,
		keyboard: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M160-200q-33 0-56.5-23.5T80-280v-400q0-33 23.5-56.5T160-760h640q33 0 56.5 23.5T880-680v400q0 33-23.5 56.5T800-200H160Zm0-80h640v-400H160v400Zm160-40h320v-80H320v80ZM200-440h80v-80h-80v80Zm120 0h80v-80h-80v80Zm120 0h80v-80h-80v80Zm120 0h80v-80h-80v80Zm120 0h80v-80h-80v80ZM200-560h80v-80h-80v80Zm120 0h80v-80h-80v80Zm120 0h80v-80h-80v80Zm120 0h80v-80h-80v80Zm120 0h80v-80h-80v80ZM160-280v-400 400Z"/></svg>`,
		keyboardAlt: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M120-160q-33 0-56.5-23.5T40-240v-480q0-33 23.5-56.5T120-800h720q33 0 56.5 23.5T920-720v480q0 33-23.5 56.5T840-160H120Zm0-80h720v-480H120v480Zm200-40h320v-80H320v80ZM200-420h80v-80h-80v80Zm160 0h80v-80h-80v80Zm160 0h80v-80h-80v80Zm160 0h80v-80h-80v80ZM200-560h80v-80h-80v80Zm160 0h80v-80h-80v80Zm160 0h80v-80h-80v80Zm160 0h80v-80h-80v80ZM120-240v-480 480Z"/></svg>`,
		keyboardArrowDown: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-344 240-584l56-56 184 184 184-184 56 56-240 240Z"/></svg>`,
		keyboardArrowUp: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-528 296-344l-56-56 240-240 240 240-56 56-184-184Z"/></svg>`,
		keyboardDoubleArrowRight: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M383-480 200-664l56-56 240 240-240 240-56-56 183-184Zm264 0L464-664l56-56 240 240-240 240-56-56 183-184Z"/></svg>`,
		keyboardTab: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M800-240v-480h80v480h-80Zm-320 0-57-56 144-144H80v-80h487L424-664l56-56 240 240-240 240Z"/></svg>`,
		kubernetes: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 777 753.918"><path style="fill:#326ce5;fill-opacity:1;stroke:none;stroke-width:0;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1" d="M386.932 178.598a48.93 48.53 0 0 0-18.751 4.745l-255.875 122.26a48.93 48.53 0 0 0-26.475 32.92L22.705 613.15a48.93 48.53 0 0 0 6.643 37.208 48.93 48.53 0 0 0 2.782 3.862l177.106 220.205a48.93 48.53 0 0 0 38.256 18.26l284.018-.065a48.93 48.53 0 0 0 38.255-18.228l177.041-220.238a48.93 48.53 0 0 0 9.458-41.07l-63.225-274.626a48.93 48.53 0 0 0-26.474-32.92L410.657 183.342a48.93 48.53 0 0 0-23.725-4.745z" transform="matrix(1.05569 0 0 1.05569 -22.672 -188.477)"/><path d="M389.467 272.057c-8.458 0-15.316 7.62-15.315 17.017 0 .144.03.282.033.425-.013 1.277-.074 2.815-.033 3.927.202 5.42 1.383 9.569 2.094 14.563 1.289 10.688 2.368 19.548 1.702 27.783-.648 3.105-2.935 5.945-4.974 7.92l-.36 6.479a199.32 199.32 0 0 0-27.685 4.254c-39.767 9.03-74.005 29.513-100.072 57.17a256.707 256.707 0 0 1-5.531-3.927c-2.734.37-5.498 1.213-9.097-.883-6.854-4.614-13.097-10.982-20.65-18.653-3.46-3.67-5.967-7.164-10.079-10.701-.934-.804-2.359-1.89-3.403-2.717-3.215-2.563-7.007-3.9-10.669-4.025-4.708-.16-9.24 1.68-12.206 5.4-5.273 6.613-3.585 16.721 3.763 22.58.075.06.154.105.23.164 1.01.818 2.246 1.867 3.174 2.552 4.363 3.222 8.35 4.871 12.697 7.429 9.16 5.656 16.753 10.347 22.776 16.002 2.352 2.507 2.763 6.925 3.077 8.836l4.908 4.385c-26.277 39.546-38.439 88.393-31.252 138.164l-6.414 1.865c-1.69 2.183-4.08 5.618-6.578 6.643-7.88 2.482-16.749 3.394-27.456 4.516-5.027.418-9.364.169-14.693 1.178-1.173.223-2.807.648-4.09.95-.045.008-.087.022-.132.032-.07.016-.161.05-.229.065-9.027 2.182-14.826 10.479-12.959 18.654 1.868 8.176 10.685 13.149 19.766 11.191.066-.015.16-.017.23-.032.102-.024.192-.073.294-.098 1.266-.278 2.852-.587 3.96-.884 5.239-1.403 9.033-3.464 13.744-5.269 10.133-3.634 18.526-6.67 26.703-7.854 3.415-.267 7.013 2.108 8.803 3.11l6.676-1.146c15.362 47.63 47.557 86.126 88.324 110.282l-2.782 6.676c1.003 2.592 2.109 6.1 1.362 8.66-2.973 7.708-8.064 15.845-13.862 24.916-2.808 4.19-5.68 7.443-8.214 12.239-.607 1.148-1.379 2.91-1.964 4.123-3.936 8.422-1.049 18.122 6.512 21.762 7.61 3.663 17.053-.2 21.14-8.64.007-.011.028-.02.033-.032.004-.009-.004-.023 0-.033.583-1.196 1.407-2.769 1.898-3.894 2.17-4.97 2.892-9.23 4.418-14.039 4.052-10.179 6.279-20.859 11.857-27.514 1.528-1.822 4.018-2.523 6.6-3.214l3.469-6.283c35.54 13.641 75.32 17.302 115.06 8.279a198.726 198.726 0 0 0 26.278-7.92c.975 1.73 2.787 5.054 3.273 5.891 2.623.854 5.487 1.294 7.82 4.745 4.174 7.13 7.028 15.566 10.505 25.754 1.527 4.808 2.28 9.068 4.451 14.04.495 1.132 1.315 2.727 1.898 3.926 4.079 8.467 13.553 12.343 21.173 8.672 7.56-3.642 10.45-13.34 6.512-21.762-.585-1.212-1.39-2.975-1.996-4.123-2.534-4.796-5.406-8.016-8.214-12.206-5.798-9.07-10.608-16.606-13.58-24.315-1.244-3.975.209-6.448 1.177-9.032-.58-.664-1.82-4.419-2.552-6.185 42.366-25.015 73.616-64.948 88.291-111.068 1.982.312 5.426.921 6.545 1.146 2.304-1.52 4.421-3.501 8.574-3.174 8.177 1.182 16.57 4.22 26.703 7.853 4.71 1.805 8.505 3.9 13.745 5.302 1.107.296 2.694.573 3.96.85.101.026.192.076.294.099.068.015.164.018.23.033 9.08 1.955 17.9-3.015 19.765-11.192 1.865-8.176-3.932-16.475-12.96-18.653-1.312-.299-3.174-.806-4.45-1.048-5.329-1.009-9.666-.76-14.693-1.178-10.707-1.122-19.576-2.034-27.456-4.516-3.213-1.246-5.499-5.07-6.61-6.643l-6.186-1.8c3.207-23.2 2.343-47.345-3.207-71.503-5.6-24.384-15.498-46.685-28.7-66.333 1.587-1.443 4.583-4.096 5.433-4.876.248-2.748.035-5.63 2.88-8.672 6.023-5.656 13.617-10.346 22.776-16.003 4.348-2.558 8.367-4.206 12.73-7.428.987-.729 2.334-1.883 3.37-2.717 7.347-5.86 9.039-15.968 3.764-22.58-5.274-6.612-15.495-7.235-22.842-1.374-1.045.828-2.464 1.908-3.403 2.716-4.112 3.537-6.651 7.031-10.112 10.701-7.553 7.672-13.796 14.072-20.65 18.686-2.97 1.729-7.32 1.13-9.293 1.014l-5.825 4.156c-33.216-34.83-78.44-57.098-127.136-61.424-.136-2.04-.314-5.73-.36-6.84-1.993-1.907-4.401-3.536-5.007-7.657-.666-8.235.447-17.095 1.735-27.783.711-4.994 1.893-9.143 2.094-14.563.046-1.232-.027-3.02-.032-4.352-.001-9.398-6.858-17.018-15.316-17.017zm-19.176 118.79-4.55 80.34-.326.164c-.306 7.187-6.22 12.926-13.483 12.926-2.975 0-5.72-.955-7.952-2.585l-.131.065-65.875-46.698c20.246-19.908 46.142-34.62 75.987-41.397a161.4 161.4 0 0 1 16.33-2.814zm38.386 0c34.843 4.286 67.067 20.064 91.76 44.245l-65.45 46.403-.229-.098c-5.809 4.243-13.994 3.19-18.522-2.487a13.412 13.412 0 0 1-2.945-7.821l-.066-.033zm-154.592 74.22 60.148 53.8-.065.327c5.429 4.72 6.23 12.91 1.701 18.588a13.5 13.5 0 0 1-7.003 4.614l-.065.262-77.1 22.253c-3.924-35.882 4.533-70.763 22.384-99.844zm270.34.033c8.936 14.486 15.703 30.664 19.732 48.204 3.98 17.329 4.98 34.627 3.338 51.345l-77.492-22.318-.065-.328c-6.94-1.896-11.204-8.955-9.589-16.035a13.39 13.39 0 0 1 4.287-7.166l-.033-.164 59.821-53.538zM377.13 523.023h24.642l15.315 19.144-5.498 23.89-22.122 10.635-22.187-10.669-5.498-23.889zm78.998 65.515c1.047-.053 2.09.041 3.108.229l.131-.164 79.75 13.483c-11.67 32.79-34.005 61.198-63.845 80.208l-30.958-74.776.098-.13c-2.844-6.608.002-14.357 6.545-17.508a13.468 13.468 0 0 1 5.17-1.342zm-133.943.327c6.086.086 11.545 4.31 12.96 10.505.661 2.9.339 5.774-.753 8.312l.229.294L303.99 682c-28.639-18.377-51.449-45.892-63.65-79.652l79.063-13.417.13.163a13.67 13.67 0 0 1 2.651-.229zm66.791 32.43c2.12-.077 4.272.358 6.316 1.342 2.68 1.29 4.751 3.323 6.054 5.76h.295l38.975 70.423a161.537 161.537 0 0 1-15.577 4.353c-29.808 6.768-59.52 4.717-86.426-4.45l38.877-70.293h.065a13.519 13.519 0 0 1 11.421-7.134z" style="color:#000;font-style:normal;font-variant:normal;font-weight:400;font-stretch:normal;font-size:medium;line-height:normal;font-family:Sans;-inkscape-font-specification:Sans;text-indent:0;text-align:start;text-decoration:none;text-decoration-line:none;letter-spacing:normal;word-spacing:normal;text-transform:none;writing-mode:lr-tb;direction:ltr;baseline-shift:baseline;text-anchor:start;display:inline;overflow:visible;visibility:visible;fill:#fff;fill-opacity:1;stroke:none;stroke-width:0;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1;marker:none;enable-background:accumulate" transform="matrix(1.05569 0 0 1.05569 -22.672 -188.477)"/></svg>`,
		label: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M160-160q-33 0-56.5-23.5T80-240v-480q0-33 23.5-56.5T160-800h440q19 0 36 8.5t28 23.5l216 288-216 288q-11 15-28 23.5t-36 8.5H160Zm0-80h440l180-240-180-240H160v480Zm220-240Z"/></svg>`,
		lightbulb2: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M400-240q-33 0-56.5-23.5T320-320v-50q-57-39-88.5-100T200-600q0-117 81.5-198.5T480-880q117 0 198.5 81.5T760-600q0 69-31.5 129.5T640-370v50q0 33-23.5 56.5T560-240H400Zm0-80h160v-92l34-24q41-28 63.5-71.5T680-600q0-83-58.5-141.5T480-800q-83 0-141.5 58.5T280-600q0 49 22.5 92.5T366-436l34 24v92Zm0 240q-17 0-28.5-11.5T360-120v-40h240v40q0 17-11.5 28.5T560-80H400Zm80-520Z"/></svg>`,
		lightMode: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-360q50 0 85-35t35-85q0-50-35-85t-85-35q-50 0-85 35t-35 85q0 50 35 85t85 35Zm0 80q-83 0-141.5-58.5T280-480q0-83 58.5-141.5T480-680q83 0 141.5 58.5T680-480q0 83-58.5 141.5T480-280ZM200-440H40v-80h160v80Zm720 0H760v-80h160v80ZM440-760v-160h80v160h-80Zm0 720v-160h80v160h-80ZM256-650l-101-97 57-59 96 100-52 56Zm492 496-97-101 53-55 101 97-57 59Zm-98-550 97-101 59 57-100 96-56-52ZM154-212l101-97 55 53-97 101-59-57Zm326-268Z"/></svg>`,
		link: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M440-280H280q-83 0-141.5-58.5T80-480q0-83 58.5-141.5T280-680h160v80H280q-50 0-85 35t-35 85q0 50 35 85t85 35h160v80ZM320-440v-80h320v80H320Zm200 160v-80h160q50 0 85-35t35-85q0-50-35-85t-85-35H520v-80h160q83 0 141.5 58.5T880-480q0 83-58.5 141.5T680-280H520Z"/></svg>`,
		list: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M280-600v-80h560v80H280Zm0 160v-80h560v80H280Zm0 160v-80h560v80H280ZM160-600q-17 0-28.5-11.5T120-640q0-17 11.5-28.5T160-680q17 0 28.5 11.5T200-640q0 17-11.5 28.5T160-600Zm0 160q-17 0-28.5-11.5T120-480q0-17 11.5-28.5T160-520q17 0 28.5 11.5T200-480q0 17-11.5 28.5T160-440Zm0 160q-17 0-28.5-11.5T120-320q0-17 11.5-28.5T160-360q17 0 28.5 11.5T200-320q0 17-11.5 28.5T160-280Z"/></svg>`,
		listAlt: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M320-280q17 0 28.5-11.5T360-320q0-17-11.5-28.5T320-360q-17 0-28.5 11.5T280-320q0 17 11.5 28.5T320-280Zm0-160q17 0 28.5-11.5T360-480q0-17-11.5-28.5T320-520q-17 0-28.5 11.5T280-480q0 17 11.5 28.5T320-440Zm0-160q17 0 28.5-11.5T360-640q0-17-11.5-28.5T320-680q-17 0-28.5 11.5T280-640q0 17 11.5 28.5T320-600Zm120 320h240v-80H440v80Zm0-160h240v-80H440v80Zm0-160h240v-80H440v80ZM200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-560H200v560Zm0-560v560-560Z"/></svg>`,
		locationOn: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-480q33 0 56.5-23.5T560-560q0-33-23.5-56.5T480-640q-33 0-56.5 23.5T400-560q0 33 23.5 56.5T480-480Zm0 294q122-112 181-203.5T720-552q0-109-69.5-178.5T480-800q-101 0-170.5 69.5T240-552q0 71 59 162.5T480-186Zm0 106Q319-217 239.5-334.5T160-552q0-150 96.5-239T480-880q127 0 223.5 89T800-552q0 100-79.5 217.5T480-80Zm0-480Z"/></svg>`,
		login: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-120v-80h280v-560H480v-80h280q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H480Zm-80-160-55-58 102-102H120v-80h327L345-622l55-58 200 200-200 200Z"/></svg>`,
		matchCase: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m131-252 165-440h79l165 440h-76l-39-112H247l-40 112h-76Zm139-176h131l-64-182h-4l-63 182Zm395 186q-51 0-81-27.5T554-342q0-44 34.5-72.5T677-443q23 0 45 4t38 11v-12q0-29-20.5-47T685-505q-23 0-42 9.5T610-468l-47-35q24-29 54.5-43t68.5-14q69 0 103 32.5t34 97.5v178h-63v-37h-4q-14 23-38 35t-53 12Zm12-54q35 0 59.5-24t24.5-56q-14-8-33.5-12.5T689-393q-32 0-50 14t-18 37q0 20 16 33t40 13Z"/></svg>`,
		menu: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M120-240v-80h720v80H120Zm0-200v-80h720v80H120Zm0-200v-80h720v80H120Z"/></svg>`,
		moreVert: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-160q-33 0-56.5-23.5T400-240q0-33 23.5-56.5T480-320q33 0 56.5 23.5T560-240q0 33-23.5 56.5T480-160Zm0-240q-33 0-56.5-23.5T400-480q0-33 23.5-56.5T480-560q33 0 56.5 23.5T560-480q0 33-23.5 56.5T480-400Zm0-240q-33 0-56.5-23.5T400-720q0-33 23.5-56.5T480-800q33 0 56.5 23.5T560-720q0 33-23.5 56.5T480-640Z"/></svg>`,
		north: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M440-80v-647L256-544l-56-56 280-280 280 280-56 57-184-184v647h-80Z"/></svg>`,
		numbers: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m240-160 40-160H120l20-80h160l40-160H180l20-80h160l40-160h80l-40 160h160l40-160h80l-40 160h160l-20 80H660l-40 160h160l-20 80H600l-40 160h-80l40-160H360l-40 160h-80Zm140-240h160l40-160H420l-40 160Z"/></svg>`,
		openInNew: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h280v80H200v560h560v-280h80v280q0 33-23.5 56.5T760-120H200Zm188-212-56-56 372-372H560v-80h280v280h-80v-144L388-332Z"/></svg>`,
		orbit: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M240-100q-58 0-99-41t-41-99q0-58 41-99t99-41q58 0 99 41t41 99q0 22-6.5 42.5T354-159v-27q30 13 62 19.5t64 6.5q134 0 227-93t93-227h80q0 83-31.5 156T763-197q-54 54-127 85.5T480-80q-45 0-88-9.5T309-118q-16 9-33.5 13.5T240-100Zm0-80q25 0 42.5-17.5T300-240q0-25-17.5-42.5T240-300q-25 0-42.5 17.5T180-240q0 25 17.5 42.5T240-180Zm240-160q-58 0-99-41t-41-99q0-58 41-99t99-41q58 0 99 41t41 99q0 58-41 99t-99 41ZM80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q45 0 88 9.5t83 28.5q16-9 33.5-13.5T720-860q58 0 99 41t41 99q0 58-41 99t-99 41q-58 0-99-41t-41-99q0-22 6.5-42.5T606-801v27q-30-13-62-19.5t-64-6.5q-134 0-227 93t-93 227H80Zm640-180q25 0 42.5-17.5T780-720q0-25-17.5-42.5T720-780q-25 0-42.5 17.5T660-720q0 25 17.5 42.5T720-660ZM240-240Zm480-480Z"/></svg>`,
		palette: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-80q-82 0-155-31.5t-127.5-86Q143-252 111.5-325T80-480q0-83 32.5-156t88-127Q256-817 330-848.5T488-880q80 0 151 27.5t124.5 76q53.5 48.5 85 115T880-518q0 115-70 176.5T640-280h-74q-9 0-12.5 5t-3.5 11q0 12 15 34.5t15 51.5q0 50-27.5 74T480-80Zm0-400Zm-220 40q26 0 43-17t17-43q0-26-17-43t-43-17q-26 0-43 17t-17 43q0 26 17 43t43 17Zm120-160q26 0 43-17t17-43q0-26-17-43t-43-17q-26 0-43 17t-17 43q0 26 17 43t43 17Zm200 0q26 0 43-17t17-43q0-26-17-43t-43-17q-26 0-43 17t-17 43q0 26 17 43t43 17Zm120 160q26 0 43-17t17-43q0-26-17-43t-43-17q-26 0-43 17t-17 43q0 26 17 43t43 17ZM480-160q9 0 14.5-5t5.5-13q0-14-15-33t-15-57q0-42 29-67t71-25h70q66 0 113-38.5T800-518q0-121-92.5-201.5T488-800q-136 0-232 93t-96 227q0 133 93.5 226.5T480-160Z"/></svg>`,
		password: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M160-440q-50 0-85-35t-35-85q0-50 35-85t85-35q50 0 85 35t35 85q0 50-35 85t-85 35ZM80-200v-80h800v80H80Zm400-240q-50 0-85-35t-35-85q0-50 35-85t85-35q50 0 85 35t35 85q0 50-35 85t-85 35Zm320 0q-50 0-85-35t-35-85q0-50 35-85t85-35q50 0 85 35t35 85q0 50-35 85t-85 35Z"/></svg>`,
		pentagon: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M298-200h364l123-369-305-213-305 213 123 369Zm-58 80L80-600l400-280 400 280-160 480H240Zm240-371Z"/></svg>`,
		person: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-480q-66 0-113-47t-47-113q0-66 47-113t113-47q66 0 113 47t47 113q0 66-47 113t-113 47ZM160-160v-112q0-34 17.5-62.5T224-378q62-31 126-46.5T480-440q66 0 130 15.5T736-378q29 15 46.5 43.5T800-272v112H160Zm80-80h480v-32q0-11-5.5-20T700-306q-54-27-109-40.5T480-360q-56 0-111 13.5T260-306q-9 5-14.5 14t-5.5 20v32Zm240-320q33 0 56.5-23.5T560-640q0-33-23.5-56.5T480-720q-33 0-56.5 23.5T400-640q0 33 23.5 56.5T480-560Zm0-80Zm0 400Z"/></svg>`,
		personEdit: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-240Zm-320 80v-112q0-34 17.5-62.5T224-378q62-31 126-46.5T480-440q37 0 73 4.5t72 14.5l-67 68q-20-3-39-5t-39-2q-56 0-111 13.5T260-306q-9 5-14.5 14t-5.5 20v32h240v80H160Zm400 40v-123l221-220q9-9 20-13t22-4q12 0 23 4.5t20 13.5l37 37q8 9 12.5 20t4.5 22q0 11-4 22.5T903-340L683-120H560Zm300-263-37-37 37 37ZM620-180h38l121-122-18-19-19-18-122 121v38Zm141-141-19-18 37 37-18-19ZM480-480q-66 0-113-47t-47-113q0-66 47-113t113-47q66 0 113 47t47 113q0 66-47 113t-113 47Zm0-80q33 0 56.5-23.5T560-640q0-33-23.5-56.5T480-720q-33 0-56.5 23.5T400-640q0 33 23.5 56.5T480-560Zm0-80Z"/></svg>`,
		pieChart: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M520-520h278q-15-110-91.5-186.5T520-798v278Zm-80 358v-636q-121 15-200.5 105.5T160-480q0 122 79.5 212.5T440-162Zm80 0q110-14 187-91t91-187H520v278Zm-40-318Zm0 400q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 155.5 31.5t127 86q54.5 54.5 86 127T880-480q0 82-31.5 155T763-197.5q-54 54.5-127 86T480-80Z"/></svg>`,
		planet: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M449-539q21 0 35.5-14.5T499-589q0-21-14.5-35.5T449-639q-21 0-35.5 14.5T399-589q0 21 14.5 35.5T449-539ZM822-80q-42 0-113-35t-152-95q-19 5-38.5 7.5T479-200q-117 0-198-81t-81-198q0-20 3-40t8-39q-59-81-94.5-151.5T81-822q0-27 15-42.5t41-15.5q26 0 67.5 18T319-801q-21 11-39 23t-35 26q-19-11-37-19t-38-17q18 38 38.5 74t43.5 71q38-54 97-85t130-31q117 0 198.5 81.5T759-479q0 71-31.5 130T642-252q35 23 71.5 44t74.5 38q-8-19-16.5-37T752-244q15-17 27-36t22-39q46 78 62.5 116.5T880-138q0 29-16 43.5T822-80ZM549-359q17 0 28.5-11.5T589-399q0-17-11.5-28.5T549-439q-17 0-28.5 11.5T509-399q0 17 11.5 28.5T549-359Zm50-140q13 0 21.5-8.5T629-529q0-13-8.5-21.5T599-559q-13 0-21.5 8.5T569-529q0 13 8.5 21.5T599-499ZM468-281q-51-44-98-91t-90-98q2 38 17 71.5t41 59.5q26 26 59 41t71 17Zm103-21q48-25 78-72.5T679-480q0-83-58.5-141T479-679q-58 0-105 30t-72 78q57 76 125 144t144 125Zm-197-73Zm117-116Z"/></svg>`,
		playArrow: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M320-200v-560l440 280-440 280Zm80-280Zm0 134 210-134-210-134v268Z"/></svg>`,
		playCircle: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m380-300 280-180-280-180v360ZM480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-320Z"/></svg>`,
		powerSettingsNew: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-84 31.5-156.5T197-763l56 56q-44 44-68.5 102T160-480q0 134 93 227t227 93q134 0 227-93t93-227q0-67-24.5-125T707-707l56-56q54 54 85.5 126.5T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm-40-360v-440h80v440h-80Z"/></svg>`,
		priorityHigh: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-120q-33 0-56.5-23.5T400-200q0-33 23.5-56.5T480-280q33 0 56.5 23.5T560-200q0 33-23.5 56.5T480-120Zm-80-240v-480h160v480H400Z"/></svg>`,
		progressActivity: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-80q-82 0-155-31.5t-127.5-86Q143-252 111.5-325T80-480q0-83 31.5-155.5t86-127Q252-817 325-848.5T480-880q17 0 28.5 11.5T520-840q0 17-11.5 28.5T480-800q-133 0-226.5 93.5T160-480q0 133 93.5 226.5T480-160q133 0 226.5-93.5T800-480q0-17 11.5-28.5T840-520q17 0 28.5 11.5T880-480q0 82-31.5 155t-86 127.5q-54.5 54.5-127 86T480-80Z"/></svg>`,
		progressBar: Q`<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M5.25 18C4.41667 18 3.70833 17.7083 3.125 17.125C2.54167 16.5417 2.25 15.8333 2.25 15V9C2.25 8.16667 2.54167 7.45833 3.125 6.875C3.70833 6.29167 4.41667 6 5.25 6H18.75C19.5833 6 20.2917 6.29167 20.875 6.875C21.4583 7.45833 21.75 8.16667 21.75 9V15C21.75 15.8333 21.4583 16.5417 20.875 17.125C20.2917 17.7083 19.5833 18 18.75 18H5.25ZM5.25 16H18.75C19.0333 16 19.2708 15.9042 19.4625 15.7125C19.6542 15.5208 19.75 15.2833 19.75 15V9C19.75 8.71667 19.6542 8.47917 19.4625 8.2875C19.2708 8.09583 19.0333 8 18.75 8H5.25C4.96667 8 4.72917 8.09583 4.5375 8.2875C4.34583 8.47917 4.25 8.71667 4.25 9V15C4.25 15.2833 4.34583 15.5208 4.5375 15.7125C4.72917 15.9042 4.96667 16 5.25 16ZM5.25 15V9H13.25V15H5.25Z" fill="currentColor"/></svg>`,
		public: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm-40-82v-78q-33 0-56.5-23.5T360-320v-40L168-552q-3 18-5.5 36t-2.5 36q0 121 79.5 212T440-162Zm276-102q41-45 62.5-100.5T800-480q0-98-54.5-179T600-776v16q0 33-23.5 56.5T520-680h-80v80q0 17-11.5 28.5T400-560h-80v80h240q17 0 28.5 11.5T600-440v120h40q26 0 47 15.5t29 40.5Z"/></svg>`,
		radioButtonChecked: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-280q83 0 141.5-58.5T680-480q0-83-58.5-141.5T480-680q-83 0-141.5 58.5T280-480q0 83 58.5 141.5T480-280Zm0 200q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93Z"/></svg>`,
		redo: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M396-200q-97 0-166.5-63T160-420q0-94 69.5-157T396-640h252L544-744l56-56 200 200-200 200-56-56 104-104H396q-63 0-109.5 40T240-420q0 60 46.5 100T396-280h284v80H396Z"/></svg>`,
		refresh: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-160q-134 0-227-93t-93-227q0-134 93-227t227-93q69 0 132 28.5T720-690v-110h80v280H520v-80h168q-32-56-87.5-88T480-720q-100 0-170 70t-70 170q0 100 70 170t170 70q77 0 139-44t87-116h84q-28 106-114 173t-196 67Z"/></svg>`,
		remove: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M200-440v-80h560v80H200Z"/></svg>`,
		rocketLaunch: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m226-559 78 33q14-28 29-54t33-52l-56-11-84 84Zm142 83 114 113q42-16 90-49t90-75q70-70 109.5-155.5T806-800q-72-5-158 34.5T492-656q-42 42-75 90t-49 90Zm155-121.5q0-33.5 23-56.5t57-23q34 0 57 23t23 56.5q0 33.5-23 56.5t-57 23q-34 0-57-23t-23-56.5ZM565-220l84-84-11-56q-26 18-52 32.5T532-299l33 79Zm313-653q19 121-23.5 235.5T708-419l20 99q4 20-2 39t-20 33L538-80l-84-197-171-171-197-84 167-168q14-14 33.5-20t39.5-2l99 20q104-104 218-147t235-24ZM157-321q35-35 85.5-35.5T328-322q35 35 34.5 85.5T327-151q-25 25-83.5 43T82-76q14-103 32-161.5t43-83.5Zm57 56q-10 10-20 36.5T180-175q27-4 53.5-13.5T270-208q12-12 13-29t-11-29q-12-12-29-11.5T214-265Z"/></svg>`,
		safari: Q`<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 66.165833 65.803795"><defs><linearGradient id="b"><stop offset="0" stop-color="#06c2e7"/><stop offset=".25000015" stop-color="#0db8ec"/><stop offset=".5000003" stop-color="#12aef1"/><stop offset=".75000012" stop-color="#1f86f9"/><stop offset="1" stop-color="#107ddd"/></linearGradient><linearGradient id="a"><stop offset="0" stop-color="#bdbdbd"/><stop offset="1" stop-color="#fff"/></linearGradient><linearGradient xlink:href="#a" id="d" x1="412.97501" x2="412.97501" y1="237.60777" y2="59.392235" gradientTransform="translate(206.79018 159.77261) scale(.35154)" gradientUnits="userSpaceOnUse"/><filter id="f" width="1.0418189" height="1.0446756" x="-.02090938" y="-.0223378" color-interpolation-filters="sRGB"><feGaussianBlur stdDeviation=".95767362"/></filter><filter id="c" width="1.096" height="1.096" x="-.048" y="-.048" color-interpolation-filters="sRGB"><feGaussianBlur stdDeviation="3.5643107"/></filter><radialGradient xlink:href="#b" id="e" cx="413.06128" cy="136.81819" r="82.125351" fx="413.06128" fy="136.81819" gradientTransform="translate(194.54473 155.58044) scale(.38143)" gradientUnits="userSpaceOnUse"/></defs><path d="M502.08277 148.5a89.107765 89.107765 0 0 1-89.10777 89.10777A89.107765 89.107765 0 0 1 323.86724 148.5 89.107765 89.107765 0 0 1 412.975 59.392235 89.107765 89.107765 0 0 1 502.08277 148.5Z" filter="url(#c)" opacity=".52999998" paint-order="markers stroke fill" transform="matrix(.33865 0 0 .3261 -106.76956 -14.47833)"/><path fill="url(#d)" stroke="#cdcdcd" stroke-linecap="round" stroke-linejoin="round" stroke-width=".09301235" d="M383.29373 211.97671a31.325188 31.325188 0 0 1-31.32519 31.32519 31.325188 31.325188 0 0 1-31.32518-31.32519 31.325188 31.325188 0 0 1 31.32518-31.32519 31.325188 31.325188 0 0 1 31.32519 31.32519z" paint-order="markers stroke fill" transform="translate(-318.88562 -180.59501)"/><path fill="url(#e)" d="M380.83911 211.97671a28.870571 28.870571 0 0 1-28.87057 28.87057 28.870571 28.870571 0 0 1-28.87057-28.87057 28.870571 28.870571 0 0 1 28.87057-28.87057 28.870571 28.870571 0 0 1 28.87057 28.87057z" paint-order="markers stroke fill" transform="translate(-318.88562 -180.59501)"/><path fill="#f4f2f3" d="M33.08292 4.01671c-.23319 0-.42092.18772-.42092.42092V9.2928c0 .2332.18773.42092.42092.42092.2332 0 .42092-.18772.42092-.42092V4.43763c0-.2332-.18772-.42092-.42092-.42092zm-2.75367.17404c-.0279-.003-.0566-.003-.0856.00035-.23194.0242-.39917.2304-.37495.46234l.21218 2.03119c.0242.23194.23041.39918.46233.37496.23195-.0242.39919-.2304.37496-.46234l-.212-2.03118c-.0212-.20295-.18177-.35637-.37695-.37532zm5.5266.002c-.19519.0188-.35578.17221-.37714.37513l-.21363 2.03102c-.0244.23192.14285.43831.37478.4627.23191.0244.43811-.14268.46251-.3746l.21364-2.03119c.0244-.23192-.14286-.43814-.37478-.46252-.029-.003-.0575-.003-.0854-.00052zm-8.3553.4082c-.028.00022-.0565.003-.085.009-.22814.0483-.37294.27089-.32464.49903l1.00552 4.74981c.0483.22814.27088.37293.49902.32464.22814-.0483.37294-.27072.32465-.49886l-1.00552-4.74998c-.0423-.19963-.21792-.33543-.41401-.3339zm11.18382.004c-.19609-.002-.3718.13394-.41419.33353l-1.00897 4.74925c-.0485.22811.0962.45076.32427.49922.22811.0485.45076-.0962.49921-.32428l1.00897-4.74926c.0485-.2281-.0962-.45076-.32427-.49921-.0285-.006-.057-.009-.085-.009zM24.801 5.36212c-.0545-.005-.11077.001-.16622.0194-.22178.0721-.34238.3085-.27031.53028l.6311 1.94236c.0721.22179.30868.34238.53046.27032.22179-.0721.3422-.30868.27013-.53046l-.63109-1.94236c-.054-.16634-.20059-.27568-.36407-.28958zm16.56765.001c-.16348.0139-.30999.12324-.36406.28957l-.63147 1.94218c-.0721.22177.0484.45837.27014.53046.22178.0721.45837-.0484.53047-.27013l.63146-1.94236c.0721-.22178-.0484-.45837-.27014-.53046-.0554-.018-.11191-.0239-.1664-.0193zm-19.23721.9759c-.0547.001-.11004.013-.16331.0367-.21298.0947-.30836.34244-.21364.55553l1.97197 4.43662c.0947.21308.34244.30836.55553.21364.21298-.0947.30854-.34244.21382-.55553l-1.97216-4.43662c-.071-.15983-.22817-.25351-.39221-.25033zm21.93693.0149c-.16403-.003-.32132.0901-.39257.24979l-1.97798 4.4339c-.095.21296-.00004.46088.21292.55589.21297.095.46088.00005.5559-.21291L44.4446 6.9467c.095-.21297.00005-.46089-.21291-.5559-.0532-.0237-.10864-.0357-.16332-.0369zM19.65353 7.6501c-.0808-.006-.16406.012-.23979.0558-.20196.1166-.27065.37302-.15406.57497l1.02115 1.76869c.1166.20196.373.27065.57496.15405.20195-.1166.27065-.37301.15406-.57497L19.9887 7.85996c-.0729-.12623-.20047-.20041-.33517-.20983zm26.85877 0c-.13468.009-.26211.0836-.33498.20983l-1.02133 1.76868c-.1166.20196-.0477.45837.15424.57497.20196.1166.45837.0479.57497-.15405l1.02114-1.76869c.1166-.20195.0479-.45837-.15406-.57497-.0757-.0437-.15916-.0614-.23998-.0558zM17.24739 9.15083c-.081.003-.16211.029-.2329.0803-.18875.13693-.23048.39911-.0935.58787l2.85086 3.92995c.13693.18876.39929.23049.58805.0936.18876-.13693.23049-.39911.0935-.58787l-2.85104-3.92995c-.0856-.11798-.22004-.17847-.35497-.17386zm31.70122.0214c-.13493-.005-.26941.0555-.35516.17331l-2.8563 3.92614c-.1372.18857-.0958.45086.0928.58805.18858.13718.45087.0959.58806-.0926l2.85613-3.92614c.13718-.18858.0957-.45086-.0928-.58805-.0707-.0514-.15176-.0778-.23272-.0807zm-33.85196 1.78231c-.10744-.006-.21708.0299-.30374.10791-.17332.15602-.18725.42109-.0312.59441l1.36648 1.51799c.15601.17332.42109.18726.59441.0312.17332-.15602.18726-.42127.0312-.59459l-1.3663-1.51781c-.078-.0867-.18339-.13351-.29085-.13916zm35.97562.003c-.10745.006-.21282.0525-.29084.13915l-1.36648 1.51763c-.15606.1733-.14224.43855.0311.59459.17329.15604.43837.14205.59441-.0312l1.36666-1.51762c.15605-.17331.14205-.43856-.0312-.59459-.0867-.078-.19611-.11354-.30357-.10791zm-38.03696 1.97705c-.10745.006-.21266.0525-.29067.13916-.15602.17332-.14207.43839.0312.59441l3.60841 3.24834c.17332.15603.43839.14207.5944-.0312.15603-.17331.14226-.43839-.0311-.59441l-3.60858-3.24834c-.0867-.078-.1963-.11356-.30376-.10791zm40.10831.0142c-.10745-.006-.21722.0298-.30393.10773l-3.61059 3.24581c-.17342.15589-.18768.42097-.0318.5944.1559.17342.42117.18751.59459.0316l3.61077-3.2458c.17342-.1559.1875-.42098.0316-.59441-.078-.0867-.18322-.13361-.29066-.13933zm-41.8225 2.18998c-.13494-.005-.26949.0558-.35515.17367-.13707.18866-.0955.4508.0932.58787l1.65224 1.20044c.18866.13708.45079.0957.58786-.093.13708-.18866.0956-.45098-.093-.58805l-1.65224-1.20044c-.0707-.0514-.15193-.0776-.23289-.0805zm43.53505.0153c-.081.003-.16211.0289-.23289.0803l-1.65297 1.19936c-.18875.13694-.2305.39929-.0936.58805.13695.18875.39912.23031.58787.0934l1.65316-1.19935c.18875-.13694.23031-.39912.0934-.58787-.0856-.11797-.22004-.17847-.35497-.17385zM9.7192 17.48992c-.13469.009-.26211.0836-.33499.20982-.1166.20195-.0479.45837.15405.57497l4.20463 2.42758c.20195.1166.45837.0479.57497-.15405.1166-.20195.0479-.45837-.15405-.57497l-4.20463-2.42759c-.0757-.0437-.15917-.0614-.23998-.0558zm46.72744 0c-.0808-.006-.16425.012-.23998.0558l-4.20463 2.42759c-.20195.1166-.27065.37302-.15405.57497.1166.20195.37302.27065.57497.15405l4.20482-2.42758c.20195-.1166.27064-.37302.15404-.57497-.0729-.12622-.20048-.20041-.33517-.20982zm-47.9386 2.50606c-.16403-.004-.32133.0899-.39258.2496-.095.21298-.00006.46091.21292.5559l1.86532.83202c.21298.095.46091.00007.5559-.2129.095-.21298-.00012-.46091-.21309-.5559l-1.86515-.83202c-.0532-.0238-.10865-.0356-.16332-.0367zm49.15794.0173c-.0547.001-.11024.013-.16351.0367l-1.86569.83057c-.21304.0949-.3083.34267-.21346.55571.0949.21304.34286.3083.5559.21346l1.8657-.83076c.21303-.0948.30811-.34267.21327-.55571-.0711-.15978-.22818-.25323-.39221-.24997zM7.42859 22.61527c-.16349.0137-.31006.12291-.36424.28921-.0722.22172.048.45839.26977.53064l4.61629 1.50418c.22171.0722.45839-.0481.53064-.26977.0722-.22172-.048-.4584-.26977-.53064L7.595 22.6347c-.0554-.0181-.11192-.024-.16641-.0194zm51.31484.018c-.0545-.005-.11078.001-.16623.0194l-4.61736 1.50092c-.22178.0721-.34223.30869-.27014.53046.0721.22177.30868.34223.53046.27014l4.61719-1.50092c.22178-.0721.3424-.30869.27032-.53046-.0541-.16633-.20077-.2757-.36424-.28957zM6.75607 25.36479c-.1961-.002-.37196.13412-.41438.33371-.0485.2281.0962.45073.32427.49922l1.99777.42455c.2281.0485.45072-.0962.49921-.32427.0485-.22811-.0962-.45074-.32427-.49922l-1.99759-.42455c-.0285-.006-.057-.009-.085-.009zm52.65462.004c-.028.00023-.0563.004-.0848.009l-1.99778.42437c-.2281.0485-.37271.27093-.32426.49904.0485.2281.2711.3729.49921.32445l1.99759-.42437c.2281-.0485.3729-.27111.32445-.49922-.0424-.19959-.21829-.33537-.41437-.33371zM6.24704 28.13046c-.1952.0187-.35587.17185-.37731.37477-.0245.2319.14232.43838.37422.46288l4.82829.51048c.2319.0245.43838-.1425.46288-.37441.0245-.2319-.1425-.43838-.37441-.46288l-4.82828-.51048c-.029-.003-.0575-.003-.0854-.00035zm53.6763.0363c-.0279-.003-.0566-.003-.0856.00035l-4.82883.50394c-.23194.0242-.39914.2304-.37496.46233.0242.23194.2304.39918.46234.37496l4.82883-.50394c.23193-.0242.39914-.2304.37496-.46234-.0212-.20294-.1816-.35634-.37678-.37532zM6.16529 30.96149c-.2332 0-.42091.18772-.42091.42092 0 .23319.18771.42091.42091.42091h2.04228c.23319 0 .4211-.18772.4211-.42091 0-.2332-.18791-.42092-.4211-.42092zm51.79298 0c-.23319 0-.42092.18772-.42092.42092.00001.23319.18773.42091.42092.42091h2.04228c.23319 0 .42092-.18772.42092-.42091 0-.2332-.18773-.42092-.42092-.42092zM11.15508 33.2561c-.0279-.003-.0564-.003-.0854.00035l-4.82902.50394c-.23194.0242-.39913.2304-.37495.46233.0242.23194.2304.39918.46233.37496l4.82902-.50394c.23194-.0242.39913-.2304.37495-.46234-.0212-.20294-.18177-.35634-.37695-.37531zm43.85314.0298c-.19521.0187-.35588.17186-.37732.37478-.0245.2319.14233.43838.37423.46288l4.82829.51048c.23191.0245.43837-.14251.46288-.37441.0245-.2319-.14251-.43838-.37441-.46288l-4.8281-.51048c-.029-.003-.0577-.003-.0856-.00035zm-46.2602 2.8436c-.028.00024-.0565.003-.085.009l-1.99777.42436c-.22811.0485-.37271.27111-.32427.49922.0485.22811.27111.37272.49922.32427l1.99777-.42419c.2281-.0485.37271-.27111.32426-.49921-.0424-.1996-.2181-.33537-.41419-.33372zm48.66925.004c-.19609-.002-.37177.13394-.41419.33353-.0485.2281.096.45074.32409.49922l1.99777.42455c.22809.0485.45073-.096.49921-.32409.0485-.2281-.0962-.45092-.32426-.4994l-1.9976-.42455c-.0285-.006-.057-.009-.085-.009zm-45.30519 1.65787c-.0545-.005-.11077.001-.16622.0194L7.3285 39.31168c-.22178.0721-.34223.30869-.27014.53046.0721.22178.30868.34222.53046.27014l4.61719-1.50092c.22178-.0721.34241-.30869.27032-.53046-.0541-.16633-.20077-.2757-.36425-.28957zm41.93713.0149c-.16349.0137-.31005.12292-.36423.28921-.0722.22173.048.4584.26977.53065l4.61628 1.50418c.22172.0722.4584-.0481.53064-.26977.0723-.22172-.048-.4584-.26977-.53065l-4.61628-1.50418c-.0554-.0181-.11191-.024-.16641-.0194zm-43.69909 3.27251c-.0547.001-.11006.0128-.16332.0365l-1.86587.83075c-.21304.0948-.30812.34267-.21328.55571.0949.21304.34268.30812.55571.21328l1.86589-.83058c.21303-.0948.30811-.34267.21327-.55571-.0711-.15978-.22837-.25323-.3924-.24997zm45.45888.016c-.16403-.004-.32133.0899-.39258.24961-.095.21297-.00006.4609.21291.55589l1.86515.83202c.21297.095.46091.00006.5559-.21291.095-.21297.00006-.4609-.21291-.55589l-1.86515-.83203c-.0532-.0238-.10864-.0356-.16332-.0367zm-41.82613.91214c-.0808-.006-.16424.012-.23998.0558L9.53826 44.4903c-.20195.1166-.27065.37302-.15405.57497.1166.20195.37302.27065.57497.15405l4.20463-2.4274c.20195-.1166.27064-.3732.15405-.57515-.0729-.12622-.2003-.20041-.33499-.20982zm38.20028 0c-.13469.009-.26229.0836-.33517.20982-.1166.20195-.0479.45855.15405.57515l4.20463 2.4274c.20196.1166.45855.0479.57515-.15405.1166-.20195.0479-.45837-.15404-.57497l-4.20482-2.42758c-.0757-.0437-.15899-.0614-.2398-.0558zm-39.24903 3.56244c-.081.003-.16211.0291-.2329.0805l-1.65296 1.19935c-.18875.13694-.2305.39912-.0936.58787.13695.18875.39912.2305.58787.0935l1.65314-1.19935c.18877-.13693.23051-.39911.0936-.58786-.0856-.11797-.22022-.17866-.35516-.17404zm40.28761.0142c-.13494-.005-.26948.0558-.35515.17367-.13708.18865-.0955.45098.0932.58805l1.65224 1.20044c.18866.13707.4508.0955.58787-.0932.13707-.18866.0956-.4508-.093-.58787l-1.65224-1.20044c-.0707-.0514-.15193-.0778-.23289-.0807zm-36.54387.14533c-.10743-.006-.21702.0298-.30374.10773l-3.61076 3.2458c-.17342.15589-.18751.42098-.0316.59441.15589.17342.42097.1875.5944.0316l3.61077-3.2458c.17342-.15589.18751-.42098.0316-.59441-.0779-.0867-.18322-.13361-.29067-.13933zm32.80012.0116c-.10745.006-.21283.0525-.29084.13915-.15603.17332-.14207.43839.0312.59441l3.60841 3.24834c.17332.15604.43857.14208.59459-.0312.15603-.17331.14207-.43839-.0312-.5944l-3.6086-3.24835c-.0867-.078-.19611-.11355-.30356-.10791zm-29.37464 3.08358c-.13493-.005-.2696.0554-.35534.1733l-2.85613 3.92614c-.13719.18858-.0959.45087.0926.58805.18857.13719.45087.0959.58805-.0927l2.85613-3.92614c.13718-.18857.0959-.45086-.0926-.58805-.0707-.0514-.15175-.0778-.23271-.0806zm25.93573.0176c-.081.003-.16211.0289-.2329.0803-.18875.13694-.23048.39911-.0936.58787l2.85086 3.92995c.13693.18876.39911.2305.58787.0936.18876-.13693.23049-.3991.0936-.58786l-2.85086-3.92996c-.0856-.11797-.22004-.17846-.35498-.17385zm-29.6228.6064c-.10745.006-.21282.0525-.29084.13915l-1.36649 1.51763c-.15605.1733-.14223.43855.0311.59459.1733.15604.43837.14205.5944-.0313l1.36666-1.51762c.15606-.1733.14206-.43856-.0312-.59459-.0867-.078-.19611-.11354-.30357-.10791zm33.33076.002c-.10745-.006-.21691.0299-.30356.10791-.17333.156-.18726.42108-.0313.5944l1.3663 1.51799c.15602.17333.42109.18726.59442.0312.17332-.15601.18726-.42126.0312-.59459l-1.36631-1.5178c-.078-.0867-.18339-.13351-.29084-.13916zm-25.65524 1.68366c-.16403-.004-.32114.0899-.39239.24961l-1.97816 4.43389c-.095.21297-.00005.46089.21292.5559.21296.095.46089.00005.55589-.21291l1.97815-4.4339c.095-.21296.00005-.46089-.21292-.55589-.0532-.0238-.10881-.0356-.16349-.0367zm17.95556.0122c-.0547.001-.11023.0128-.1635.0365-.21297.0947-.30836.34244-.21363.55553l1.97196 4.43662c.0947.21297.34262.30836.55571.21364.21298-.0947.30836-.34244.21364-.55553l-1.97197-4.43662c-.071-.15973-.22818-.25329-.39221-.25015zM20.61581 52.5046c-.13468.009-.26212.0836-.33498.20982l-1.02115 1.76869c-.11659.20195-.0479.45837.15406.57497.20195.1166.45837.0479.57496-.15405l1.02115-1.76869c.11659-.20195.0479-.45837-.15406-.57497-.0757-.0437-.15916-.0614-.23998-.0558zm24.93421 0c-.0808-.006-.16406.0121-.23979.0558-.20195.1166-.27065.37302-.15405.57497l1.02114 1.76869c.1166.20195.37302.27064.57496.15405.20196-.1166.27066-.37302.15406-.57497l-1.02114-1.76869c-.0729-.12622-.20049-.20041-.33518-.20982zm-17.0545.0634c-.19609-.002-.3718.13394-.41419.33354l-1.00897 4.74926c-.0485.2281.0962.45076.32427.49921.22811.0485.45076-.0962.49922-.32427l1.00896-4.74926c.0485-.2281-.0962-.45076-.32427-.49921-.0285-.006-.057-.009-.085-.009zm9.1599.003c-.028.00022-.0563.003-.0848.009-.22814.0483-.37294.27071-.32465.49885l1.00553 4.74999c.0483.22814.27088.37293.49903.32464.22814-.0483.37293-.27089.32464-.49903l-1.0057-4.74965c-.0423-.19963-.21793-.33543-.41402-.33391zm-4.5725.47905c-.23319 0-.42092.18772-.42092.42092v4.85517c0 .2332.18773.42092.42092.42092.2332 0 .42092-.18772.42092-.42092v-4.85517c0-.2332-.18772-.42092-.42092-.42092zm-7.72657 1.56886c-.16347.0139-.31017.12324-.36423.28957l-.63129 1.94236c-.0721.22178.0484.45837.27014.53047.22177.0721.45836-.0486.53046-.27032l.63128-1.94218c.0721-.22177-.0484-.45836-.27013-.53046-.0554-.018-.11173-.024-.16623-.0194zm15.44987.001c-.0545-.005-.11078.001-.16622.0193-.22178.0721-.34238.30868-.27033.53047l.63111 1.94235c.0721.22179.30868.3422.53046.27014.22178-.0721.34238-.3085.27032-.53028l-.63128-1.94236c-.0541-.16634-.20058-.27568-.36406-.28957zm-10.36543 1.08181c-.1952.0188-.356.17203-.37732.37496l-.21346 2.03119c-.0244.23192.14268.43812.3746.46252.23192.0244.4383-.14268.4627-.3746l.21345-2.03101c.0244-.23192-.14268-.4383-.37458-.4627-.029-.003-.0575-.003-.0854-.00035zm5.26736.002c-.0279-.003-.0566-.003-.0856.00035-.23193.0242-.39917.2304-.37495.46233l.21218 2.03138c.0242.23193.2304.399.46234.37478.23193-.0242.39918-.23041.37496-.46234l-.212-2.03119c-.0212-.20295-.18178-.35637-.37697-.37533z" paint-order="markers stroke fill"/><path d="m469.09621 100.6068-65.50955 38.06124-41.41979 65.20654 60.59382-44.88117z" filter="url(#f)" opacity=".40900005" paint-order="markers stroke fill" transform="translate(-112.09544 -20.8224) scale(.35154)"/><path fill="#ff5150" d="m36.3834003 34.83806178-6.60095092-6.91272438 23.41607429-15.75199774z" paint-order="markers stroke fill"/><path fill="#f1f1f1" d="m36.38339038 34.83805895-6.60095092-6.91272438-16.81512624 22.66471911z" paint-order="markers stroke fill"/><path d="m12.96732 50.59006 23.41607-15.75201 16.81513-22.66472z" opacity=".243"/></svg>`,
		save: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M840-680v480q0 33-23.5 56.5T760-120H200q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h480l160 160Zm-80 34L646-760H200v560h560v-446ZM480-240q50 0 85-35t35-85q0-50-35-85t-85-35q-50 0-85 35t-35 85q0 50 35 85t85 35ZM240-560h360v-160H240v160Zm-40-86v446-560 114Z"/></svg>`,
		saveAs: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h480l160 160v212q-19-8-39.5-10.5t-40.5.5v-169L647-760H200v560h240v80H200Zm0-640v560-560ZM520-40v-123l221-220q9-9 20-13t22-4q12 0 23 4.5t20 13.5l37 37q8 9 12.5 20t4.5 22q0 11-4 22.5T863-260L643-40H520Zm300-263-37-37 37 37ZM580-100h38l121-122-18-19-19-18-122 121v38Zm141-141-19-18 37 37-18-19ZM240-560h360v-160H240v160Zm240 320h4l116-115v-5q0-50-35-85t-85-35q-50 0-85 35t-35 85q0 50 35 85t85 35Z"/></svg>`,
		scatterPlot: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M680-120q-66 0-113-47t-47-113q0-66 47-113t113-47q66 0 113 47t47 113q0 66-47 113t-113 47Zm0-80q33 0 56.5-23.5T760-280q0-33-23.5-56.5T680-360q-33 0-56.5 23.5T600-280q0 33 23.5 56.5T680-200Zm-400-40q-66 0-113-47t-47-113q0-66 47-113t113-47q66 0 113 47t47 113q0 66-47 113t-113 47Zm0-80q33 0 56.5-23.5T360-400q0-33-23.5-56.5T280-480q-33 0-56.5 23.5T200-400q0 33 23.5 56.5T280-320Zm160-240q-66 0-113-47t-47-113q0-66 47-113t113-47q66 0 113 47t47 113q0 66-47 113t-113 47Zm0-80q33 0 56.5-23.5T520-720q0-33-23.5-56.5T440-800q-33 0-56.5 23.5T360-720q0 33 23.5 56.5T440-640Zm240 360ZM280-400Zm160-320Z"/></svg>`,
		screenRecord: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M158-242q-37-50-57.5-110.5T80-480q0-67 20-127t57-110l58 57q-26 38-40.5 83.5T160-480q0 51 14.5 97t40.5 84l-57 57ZM480-80q-67 0-127-20t-110-57l57-58q38 26 83.5 40.5T480-160q51 0 96.5-14.5T660-215l57 58q-50 37-110 57T480-80Zm322-162-57-57q26-38 40.5-84t14.5-97q0-51-14.5-96.5T745-660l58-57q37 50 57 110t20 127q0 67-20.5 127.5T802-242ZM299-745l-57-57q50-37 110.5-57.5T480-880q68 0 128 20.5T718-802l-57 57q-38-26-84-40.5T480-800q-51 0-97 14.5T299-745Zm181 465q-83 0-141.5-58.5T280-480q0-83 58.5-141.5T480-680q83 0 141.5 58.5T680-480q0 83-58.5 141.5T480-280Z"/></svg>`,
		schedule: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m612-292 56-56-148-148v-184h-80v216l172 172ZM480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-400Zm0 320q133 0 226.5-93.5T800-480q0-133-93.5-226.5T480-800q-133 0-226.5 93.5T160-480q0 133 93.5 226.5T480-160Z"/></svg>`,
		search: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M784-120 532-372q-30 24-69 38t-83 14q-109 0-184.5-75.5T120-580q0-109 75.5-184.5T380-840q109 0 184.5 75.5T640-580q0 44-14 83t-38 69l252 252-56 56ZM380-400q75 0 127.5-52.5T560-580q0-75-52.5-127.5T380-760q-75 0-127.5 52.5T200-580q0 75 52.5 127.5T380-400Z"/></svg>`,
		send: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M120-160v-640l760 320-760 320Zm80-120 474-200-474-200v140l240 60-240 60v140Zm0 0v-400 400Z"/></svg>`,
		settings: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m370-80-16-128q-13-5-24.5-12T307-235l-119 50L78-375l103-78q-1-7-1-13.5v-27q0-6.5 1-13.5L78-585l110-190 119 50q11-8 23-15t24-12l16-128h220l16 128q13 5 24.5 12t22.5 15l119-50 110 190-103 78q1 7 1 13.5v27q0 6.5-2 13.5l103 78-110 190-118-50q-11 8-23 15t-24 12L590-80H370Zm70-80h79l14-106q31-8 57.5-23.5T639-327l99 41 39-68-86-65q5-14 7-29.5t2-31.5q0-16-2-31.5t-7-29.5l86-65-39-68-99 42q-22-23-48.5-38.5T533-694l-13-106h-79l-14 106q-31 8-57.5 23.5T321-633l-99-41-39 68 86 64q-5 15-7 30t-2 32q0 16 2 31t7 30l-86 65 39 68 99-42q22 23 48.5 38.5T427-266l13 106Zm42-180q58 0 99-41t41-99q0-58-41-99t-99-41q-59 0-99.5 41T342-480q0 58 40.5 99t99.5 41Zm-2-140Z"/></svg>`,
		settingsApplications: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M440-280h80l12-60q12-5 22.5-10.5T576-364l58 18 40-68-46-40q2-14 2-26t-2-26l46-40-40-68-58 18q-11-8-21.5-13.5T532-620l-12-60h-80l-12 60q-12 5-22.5 10.5T384-596l-58-18-40 68 46 40q-2 14-2 26t2 26l-46 40 40 68 58-18q11 8 21.5 13.5T428-340l12 60Zm40-120q-33 0-56.5-23.5T400-480q0-33 23.5-56.5T480-560q33 0 56.5 23.5T560-480q0 33-23.5 56.5T480-400ZM200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-560H200v560Zm0-560v560-560Z"/></svg>`,
		showChart: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m140-220-60-60 300-300 160 160 284-320 56 56-340 384-160-160-240 240Z"/></svg>`,
		south: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-80 200-360l56-56 184 183v-647h80v647l184-184 56 57L480-80Z"/></svg>`,
		sparkles: Q`<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M9.813 15.904 9 18.75l-.813-2.846a4.5 4.5 0 0 0-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 0 0 3.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 0 0 3.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 0 0-3.09 3.09ZM18.259 8.715 18 9.75l-.259-1.035a3.375 3.375 0 0 0-2.455-2.456L14.25 6l1.036-.259a3.375 3.375 0 0 0 2.455-2.456L18 2.25l.259 1.035a3.375 3.375 0 0 0 2.456 2.456L21.75 6l-1.035.259a3.375 3.375 0 0 0-2.456 2.456ZM16.894 20.567 16.5 21.75l-.394-1.183a2.25 2.25 0 0 0-1.423-1.423L13.5 18.75l1.183-.394a2.25 2.25 0 0 0 1.423-1.423l.394-1.183.394 1.183a2.25 2.25 0 0 0 1.423 1.423l1.183.394-1.183.394a2.25 2.25 0 0 0-1.423 1.423Z" /></svg>`,
		speed: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M418-340q24 24 62 23.5t56-27.5l224-336-336 224q-27 18-28.5 55t22.5 61Zm62-460q59 0 113.5 16.5T696-734l-76 48q-33-17-68.5-25.5T480-720q-133 0-226.5 93.5T160-400q0 42 11.5 83t32.5 77h552q23-38 33.5-79t10.5-85q0-36-8.5-70T766-540l48-76q30 47 47.5 100T880-406q1 57-13 109t-41 99q-11 18-30 28t-40 10H204q-21 0-40-10t-30-28q-26-45-40-95.5T80-400q0-83 31.5-155.5t86-127Q252-737 325-768.5T480-800Zm7 313Z"/></svg>`,
		spring: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 417 417"><path fill="#6cb52d" d="M366.9,29c-5.8,14.1-13.3,26.6-21.6,37.8c-36.6-37.3-87.8-61-144.3-61C90,5.8-0.7,96-0.7,207.4 c0,58.2,24.9,110.6,64.4,147.6l7.5,6.7c34.9,29.5,80.3,47.4,129.7,47.4c106,0,193.3-82.7,200.8-187.1 C407.7,171.3,392.3,106.4,366.9,29z"/><path fill="white" d="M92.9,356.7c-5.8,7.5-16.6,8.3-24.1,2.5s-8.3-16.6-2.5-24.1s16.6-8.3,24.1-2.5 C97.5,338.4,98.7,349.2,92.9,356.7z"/><path fill="white" d="M365.7,296.4c-49.5,66.1-155.9,43.7-223.7,47c0,0-12.1,0.8-24.1,2.5c0,0,4.6-2.1,10.4-4.2 c47.8-16.6,70.3-20,99.4-34.9c54.5-27.9,108.9-89,119.8-152.2c-20.8,60.7-84,113.1-141.4,134.3c-39.5,14.6-110.6,28.7-110.6,28.7 l-2.9-1.7c-48.2-23.7-49.9-128.5,38.3-162.2c38.7-15,75.3-6.7,117.3-16.6c44.5-10.4,96.1-43.7,116.8-87.3 C388.1,120.1,416.4,229,365.7,296.4z"/></svg>`,
		springBoot: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 510 457.8"><g><path d="M503.5,201.4L403,27.5C394.3,12.4,372.9,0,355.4,0H154.6c-17.4,0-38.9,12.4-47.6,27.5L6.6,201.4    c-8.7,15.1-8.7,39.8,0,54.9l100.4,174c8.7,15.1,30.1,27.5,47.6,27.5h200.9c17.4,0,38.8-12.4,47.6-27.5l100.4-174    C512.2,241.2,512.2,216.5,503.5,201.4z M233.3,96.2c0-11.4,9.3-20.7,20.7-20.7c11.4,0,20.7,9.3,20.7,20.7v123.7    c0,11.4-9.3,20.7-20.7,20.7c-11.4,0-20.7-9.3-20.7-20.7l0,0V96.2z M254,360.3c-77.4,0-140.4-63-140.4-140.4 c0.1-44.4,21.1-86.1,56.7-112.7c8.2-6.1,19.7-4.4,25.8,3.8s4.4,19.7-3.8,25.8l0,0c-45.9,34.1-55.5,99-21.4,144.9 s99,55.5,144.9,21.4c26.3-19.5,41.8-50.4,41.8-83.2c-0.1-32.9-15.7-63.8-42.2-83.4c-8.2-6-9.9-17.6-3.9-25.8s17.6-9.9,25.8-3.9 c35.9,26.5,57,68.5,57.1,113.1C394.4,297.4,331.4,360.3,254,360.3z" fill="#6DB33F"/></g></svg>`,
		springData: Q`<svg xmlns="http://www.w3.org/2000/svg" id="Layer_1" data-name="Layer 1" viewBox="0 0 116.88 145.97"><defs><style>.cls-1{fill:#6db33f;}</style></defs><title>logo-data</title><path class="cls-1" d="M58.33,101.79C29.54,101.79,17,99.42,0,95.46v44l.8.2c16.4,4,28.8,6.32,57.53,6.32s41.35-2.38,57.74-6.37l.81-.2v-44C99.93,99.4,87.27,101.79,58.33,101.79Z"/><path class="cls-1" d="M58.33,0C29.6,0,17.2,2.36.8,6.32l-.8.2V53c17-4,29.54-6.33,58.33-6.33s41.6,2.39,58.55,6.39V6.57l-.81-.2C99.68,2.38,87.21,0,58.33,0Z"/><path class="cls-1" d="M116.88,55.58l-.81-.2C99.68,51.39,87.21,49,58.33,49S17.2,51.37.8,55.32l-.8.2V92.94l.8.19c16.4,4,28.8,6.33,57.53,6.33s41.35-2.39,57.74-6.38l.81-.2Z"/></svg>`,
		springSecurity: Q`<svg xmlns="http://www.w3.org/2000/svg" id="Layer_1" data-name="Layer 1" viewBox="0 0 108.08 150.97"><defs><style>.cls-1{fill:#6bb344;}</style></defs><title>logo-security</title><path class="cls-1" d="M108.08,13,54,0,0,13V54.6H28.67a23.94,23.94,0,0,0,0,6H0V80.14C0,125,54,151,54,151s54-26,54-70.83V60.62H79.4a22.75,22.75,0,0,0,0-6h28.68ZM54,77.15A19.54,19.54,0,1,1,73.58,57.61,19.54,19.54,0,0,1,54,77.15Z"/><path class="cls-1" d="M54,48.34a5.06,5.06,0,0,0-2.32,9.56v1.31l1.49,1.49v1l1,1v1l-.88.88.94,1.55v1l-1,1.19,1.4,1.4,1.55-1.55V58A5.06,5.06,0,0,0,54,48.34Zm0,5.26a1.88,1.88,0,1,1,1.88-1.88A1.88,1.88,0,0,1,54,53.6Z"/></svg>`,
		square: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M120-120v-720h720v720H120Zm80-80h560v-560H200v560Zm0 0v-560 560Z"/></svg>`,
		star: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m354-287 126-76 126 77-33-144 111-96-146-13-58-136-58 135-146 13 111 97-33 143ZM233-120l65-281L80-590l288-25 112-265 112 265 288 25-218 189 65 281-247-149-247 149Zm247-350Z"/></svg>`,
		stars2: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m354-287 126-76 126 77-33-144 111-96-146-13-58-136-58 135-146 13 111 97-33 143ZM233-120l65-281L80-590l288-25 112-265 112 265 288 25-218 189 65 281-247-149-247 149Zm457-560 21-89-71-59 94-8 36-84 36 84 94 8-71 59 21 89-80-47-80 47ZM480-481Z"/></svg>`,
		steppers: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M200-360q-50 0-85-35t-35-85q0-50 35-85t85-35q50 0 85 35t35 85q0 50-35 85t-85 35Zm0-80q17 0 28.5-11.5T240-480q0-17-11.5-28.5T200-520q-17 0-28.5 11.5T160-480q0 17 11.5 28.5T200-440Zm280 80q-50 0-85-35t-35-85q0-50 35-85t85-35q50 0 85 35t35 85q0 50-35 85t-85 35Zm0-80q17 0 28.5-11.5T520-480q0-17-11.5-28.5T480-520q-17 0-28.5 11.5T440-480q0 17 11.5 28.5T480-440Zm280 80q-50 0-85-35t-35-85q0-50 35-85t85-35q50 0 85 35t35 85q0 50-35 85t-85 35Z"/></svg>`,
		stop: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M320-640v320-320Zm-80 400v-480h480v480H240Zm80-80h320v-320H320v320Z"/></svg>`,
		stopCircle: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M320-320h320v-320H320v320ZM480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-320Z"/></svg>`,
		subdirectoryArrowRight: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m560-120-57-57 144-143H200v-480h80v400h367L503-544l56-57 241 241-240 240Z"/></svg>`,
		swapHoriz: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M280-160 80-360l200-200 56 57-103 103h287v80H233l103 103-56 57Zm400-240-56-57 103-103H440v-80h287L624-743l56-57 200 200-200 200Z"/></svg>`,
		tab: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M160-240h640v-320H520v-160H160v480Zm0 80q-33 0-56.5-23.5T80-240v-480q0-33 23.5-56.5T160-800h640q33 0 56.5 23.5T880-720v480q0 33-23.5 56.5T800-160H160Zm0-80v-480 480Z"/></svg>`,
		table: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm240-240H200v160h240v-160Zm80 0v160h240v-160H520Zm-80-80v-160H200v160h240Zm80 0h240v-160H520v160ZM200-680h560v-80H200v80Z"/></svg>`,
		tabs: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-280H200v280Zm0-360h560v-200H200v200Zm280-80h240v-80H480v80Zm-280 80v-200 200Z"/></svg>`,
		terminal: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M160-160q-33 0-56.5-23.5T80-240v-480q0-33 23.5-56.5T160-800h640q33 0 56.5 23.5T880-720v480q0 33-23.5 56.5T800-160H160Zm0-80h640v-400H160v400Zm140-40-56-56 103-104-104-104 57-56 160 160-160 160Zm180 0v-80h240v80H480Z"/></svg>`,
		textFormat: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M200-200v-80h560v80H200Zm76-160 164-440h80l164 440h-76l-38-112H392l-40 112h-76Zm138-176h132l-64-182h-4l-64 182Z"/></svg>`,
		thermostatCarbon: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-79q-16 0-30.5-6T423-102L102-423q-11-12-17-26.5T79-480q0-16 6-31t17-26l321-321q12-12 26.5-17.5T480-881q16 0 31 5.5t26 17.5l321 321q12 11 17.5 26t5.5 31q0 16-5.5 30.5T858-423L537-102q-11 11-26 17t-31 6Zm0-80 321-321-321-321-321 321 321 321Zm0-321Z"/></svg>`,
		thumbnailBar: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M160-160q-33 0-56.5-23.5T80-240v-480q0-33 23.5-56.5T160-800h640q33 0 56.5 23.5T880-720v480q0 33-23.5 56.5T800-160H160Zm240-80h400v-480H400v480Zm-80 0v-480H160v480h160Zm-160 0v-480 480Zm160 0h80-80Zm0-480h80-80Z"/></svg>`,
		title: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M420-160v-520H200v-120h560v120H540v520H420Z"/></svg>`,
		translate: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m476-80 182-480h84L924-80h-84l-43-122H603L560-80h-84ZM160-200l-56-56 202-202q-35-35-63.5-80T190-640h84q20 39 40 68t48 58q33-33 68.5-92.5T484-720H40v-80h280v-80h80v80h280v80H564q-21 72-63 148t-83 116l96 98-30 82-122-125-202 201Zm468-72h144l-72-204-72 204Z"/></svg>`,
		trendingUp: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m136-240-56-56 296-298 160 160 208-206H640v-80h240v240h-80v-104L536-320 376-480 136-240Z"/></svg>`,
		toolbar: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-520h560v-120H200v120Zm560 80H200v360h560v-360Zm-560-80v80-80Zm0 0v-120 120Zm0 80v360-360Z"/></svg>`,
		topPanelClose: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-460 320-300h320L480-460ZM200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm560-520v-120H200v120h560Zm-560 80v360h560v-360H200Zm0-80v-120 120Z"/></svg>`,
		topPanelOpen: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m480-300 160-160H320l160 160ZM200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm560-520v-120H200v120h560Zm-560 80v360h560v-360H200Zm0-80v-120 120Z"/></svg>`,
		tune: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M440-120v-240h80v80h320v80H520v80h-80Zm-320-80v-80h240v80H120Zm160-160v-80H120v-80h160v-80h80v240h-80Zm160-80v-80h400v80H440Zm160-160v-240h80v80h160v80H680v80h-80Zm-480-80v-80h400v80H120Z"/></svg>`,
		turnLeft: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M600-160v-360H272l64 64-56 56-160-160 160-160 56 56-64 64h328q33 0 56.5 23.5T680-520v360h-80Z"/></svg>`,
		uiComponents: Q`<svg viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg"> <path d="M21.0314 29.125C26.5376 29.125 31 24.7484 31 19.3507C31 13.9531 26.5376 9.5765 21.0314 9.5765C15.5264 9.5765 11.0628 13.9519 11.0628 19.3507C11.0628 24.7484 15.5252 29.125 21.0314 29.125Z" fill="#FF707A"></path> <path d="M8.83154 4.00545L1.30913 16.786C1.15796 17.0422 1.0597 17.3253 1.01998 17.6189C0.980267 17.9126 0.999878 18.2111 1.07769 18.4973C1.15551 18.7835 1.28999 19.0517 1.47343 19.2866C1.65687 19.5215 1.88564 19.7185 2.14662 19.8662C2.49662 20.063 2.89161 20.1688 3.29411 20.17H18.3477C18.7511 20.1702 19.1475 20.0662 19.4974 19.8685C19.8472 19.6708 20.1382 19.3862 20.3414 19.0432C20.5432 18.7005 20.6495 18.3114 20.6495 17.9152C20.6495 17.5191 20.5432 17.13 20.3414 16.7873L12.819 4.00545C12.6171 3.66108 12.3264 3.37523 11.9764 3.17674C11.6263 2.97826 11.2293 2.87418 10.8253 2.875C10.4215 2.8753 10.0249 2.97985 9.675 3.17823C9.32514 3.3766 9.0343 3.66184 8.83154 4.00545Z" fill="#00B4F0"></path> <path d="M20.3414 16.7873L16.6839 10.5692C15.0027 11.3674 13.5833 12.6145 12.5877 14.1684C11.5941 15.7203 11.0655 17.5165 11.0628 19.3507C11.0628 19.6287 11.0628 19.8994 11.109 20.17H18.3477C18.7511 20.1702 19.1475 20.0662 19.4974 19.8685C19.8472 19.6708 20.1382 19.3862 20.3414 19.0432C20.5432 18.7005 20.6495 18.3114 20.6495 17.9152C20.6495 17.5191 20.5432 17.13 20.3414 16.7873Z" fill="#5748FF"></path></svg>`,
		undo: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M280-200v-80h284q63 0 109.5-40T720-420q0-60-46.5-100T564-560H312l104 104-56 56-200-200 200-200 56 56-104 104h252q97 0 166.5 63T800-420q0 94-69.5 157T564-200H280Z"/></svg>`,
		unfoldLess: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m356-160-56-56 180-180 180 180-56 56-124-124-124 124Zm124-404L300-744l56-56 124 124 124-124 56 56-180 180Z"/></svg>`,
		unfoldMore: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-120 300-300l58-58 122 122 122-122 58 58-180 180ZM358-598l-58-58 180-180 180 180-58 58-122-122-122 122Z"/></svg>`,
		upgrade: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M280-160v-80h400v80H280Zm160-160v-327L336-544l-56-56 200-200 200 200-56 56-104-103v327h-80Z"/></svg>`,
		vaadin: Q`<svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path d="M3 3C2.55 3 2.25 3.3 2.25 3.75V5.625C2.25 7.05 3.45 8.25 4.875 8.25H10.1997C10.7997 8.25 11.25 8.70029 11.25 9.30029V9.75C11.25 10.2 11.55 10.5 12 10.5C12.45 10.5 12.75 10.2 12.75 9.75V9.30029C12.75 8.70029 13.2003 8.25 13.8003 8.25H19.125C20.55 8.25 21.75 7.05 21.75 5.625V3.75C21.75 3.3 21.45 3 21 3C20.55 3 20.25 3.3 20.25 3.75V4.19971C20.25 4.79971 19.7997 5.25 19.1997 5.25H14.25C12.975 5.25 12 6.225 12 7.5C12 6.225 11.025 5.25 9.75 5.25H4.80029C4.20029 5.25 3.75 4.79971 3.75 4.19971V3.75C3.75 3.3 3.45 3 3 3ZM7.76367 11.2705C7.62187 11.2834 7.48184 11.3244 7.35059 11.3994C6.82559 11.6994 6.59941 12.3744 6.89941 12.8994L11.0244 20.3994C11.1744 20.7744 11.625 21 12 21C12.375 21 12.8256 20.7744 12.9756 20.3994L17.1006 12.8994C17.4006 12.3744 17.1744 11.6994 16.6494 11.3994C16.1244 11.0994 15.4494 11.3256 15.1494 11.8506L12 17.5503L8.85059 11.8506C8.62559 11.4568 8.18906 11.2318 7.76367 11.2705Z" fill="currentColor"/></svg>`,
		viewComfy: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M80-160v-640h800v640H80Zm720-360v-200H160v200h640ZM400-240h400v-200H400v200Zm-240 0h160v-200H160v200Z"/></svg>`,
		verifiedUser: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m438-338 226-226-57-57-169 169-84-84-57 57 141 141Zm42 258q-139-35-229.5-159.5T160-516v-244l320-120 320 120v244q0 152-90.5 276.5T480-80Zm0-84q104-33 172-132t68-220v-189l-240-90-240 90v189q0 121 68 220t172 132Zm0-316Z"/></svg>`,
		visibility: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M480-320q75 0 127.5-52.5T660-500q0-75-52.5-127.5T480-680q-75 0-127.5 52.5T300-500q0 75 52.5 127.5T480-320Zm0-72q-45 0-76.5-31.5T372-500q0-45 31.5-76.5T480-608q45 0 76.5 31.5T588-500q0 45-31.5 76.5T480-392Zm0 192q-146 0-266-81.5T40-500q54-137 174-218.5T480-800q146 0 266 81.5T920-500q-54 137-174 218.5T480-200Zm0-300Zm0 220q113 0 207.5-59.5T832-500q-50-101-144.5-160.5T480-720q-113 0-207.5 59.5T128-500q50 101 144.5 160.5T480-280Z"/></svg>`,
		visibilityOff: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m644-428-58-58q9-47-27-88t-93-32l-58-58q17-8 34.5-12t37.5-4q75 0 127.5 52.5T660-500q0 20-4 37.5T644-428Zm128 126-58-56q38-29 67.5-63.5T832-500q-50-101-143.5-160.5T480-720q-29 0-57 4t-55 12l-62-62q41-17 84-25.5t90-8.5q151 0 269 83.5T920-500q-23 59-60.5 109.5T772-302Zm20 246L624-222q-35 11-70.5 16.5T480-200q-151 0-269-83.5T40-500q21-53 53-98.5t73-81.5L56-792l56-56 736 736-56 56ZM222-624q-29 26-53 57t-41 67q50 101 143.5 160.5T480-280q20 0 39-2.5t39-5.5l-36-38q-11 3-21 4.5t-21 1.5q-75 0-127.5-52.5T300-500q0-11 1.5-21t4.5-21l-84-82Zm319 93Zm-151 75Z"/></svg>`,
		vsCode: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 72 73" fill="none"><mask id="mask0_1152_115027" style="mask-type:alpha" maskUnits="userSpaceOnUse" x="3" y="3" width="66" height="67"><path fill-rule="evenodd" clip-rule="evenodd" d="M49.7662 69.1663C50.803 69.5702 51.9853 69.5443 53.0317 69.0408L66.5847 62.5194C68.0089 61.8341 68.9145 60.3928 68.9145 58.8115V14.5919C68.9145 13.0107 68.0089 11.5694 66.5847 10.8841L53.0317 4.36246C51.6584 3.70161 50.0511 3.86348 48.8457 4.73978C48.6735 4.86496 48.5095 5.00473 48.3556 5.15876L22.41 28.8294L11.1086 20.2508C10.0565 19.4522 8.58501 19.5176 7.60792 20.4064L3.9832 23.7036C2.78803 24.7908 2.78666 26.6711 3.98024 27.76L13.7812 36.7016L3.98024 45.6432C2.78666 46.7322 2.78803 48.6124 3.9832 49.6996L7.60792 52.9968C8.58501 53.8856 10.0565 53.9511 11.1086 53.1525L22.41 44.5738L48.3556 68.2445C48.766 68.6552 49.2479 68.9644 49.7662 69.1663ZM52.4674 21.7578L32.7806 36.7016L52.4674 51.6455V21.7578Z" fill="white"/></mask><g mask="url(#mask0_1152_115027)"><g filter="url(#filter0_d_1152_115027)"><path d="M66.5821 10.8957L53.0185 4.36504C51.4485 3.60913 49.5723 3.92799 48.3401 5.16011L3.93782 45.6446C2.74351 46.7335 2.74488 48.6138 3.94078 49.701L7.5677 52.9982C8.54539 53.887 10.0178 53.9524 11.0705 53.1538L64.5411 12.5899C66.335 11.229 68.9116 12.5085 68.9116 14.7601V14.6027C68.9116 13.0221 68.0062 11.5813 66.5821 10.8957Z" fill="#0065A9"/></g><g filter="url(#filter1_d_1152_115027)"><path d="M66.5821 62.5092L53.0185 69.0398C51.4485 69.7957 49.5723 69.4769 48.3401 68.2447L3.93782 27.7603C2.74351 26.6714 2.74488 24.7911 3.94078 23.7039L7.5677 20.4067C8.54539 19.5179 10.0178 19.4524 11.0705 20.251L64.5411 60.815C66.335 62.1758 68.9116 60.8964 68.9116 58.6448V58.8022C68.9116 60.3827 68.0062 61.8235 66.5821 62.5092Z" fill="#007ACC"/></g><g filter="url(#filter2_d_1152_115027)"><path d="M53.0227 69.0408C51.4522 69.796 49.576 69.4767 48.3438 68.2445C49.862 69.7627 52.458 68.6874 52.458 66.5403V6.86296C52.458 4.71579 49.862 3.64049 48.3438 5.15876C49.576 3.92652 51.4522 3.60721 53.0227 4.36246L66.584 10.8841C68.009 11.5694 68.9152 13.0107 68.9152 14.5919V58.8115C68.9152 60.3928 68.009 61.8341 66.584 62.5194L53.0227 69.0408Z" fill="#1F9CF0"/></g><g style="mix-blend-mode:overlay" opacity="0.25"><path fill-rule="evenodd" clip-rule="evenodd" d="M49.7232 69.1663C50.76 69.5702 51.9423 69.5443 52.9888 69.0408L66.5417 62.5194C67.9659 61.8341 68.8715 60.3928 68.8715 58.8115V14.5919C68.8715 13.0107 67.9659 11.5694 66.5418 10.8841L52.9887 4.36246C51.6154 3.70161 50.0081 3.86348 48.8027 4.73978C48.6305 4.86496 48.4665 5.00473 48.3126 5.15876L22.367 28.8294L11.0656 20.2508C10.0136 19.4522 8.54205 19.5176 7.56495 20.4064L3.94023 23.7036C2.74506 24.7908 2.74369 26.6711 3.93727 27.76L13.7382 36.7016L3.93727 45.6432C2.74369 46.7322 2.74506 48.6124 3.94023 49.6996L7.56495 52.9968C8.54205 53.8856 10.0136 53.9511 11.0656 53.1525L22.367 44.5738L48.3126 68.2445C48.723 68.6552 49.205 68.9644 49.7232 69.1663ZM52.4244 21.7578L32.7377 36.7016L52.4244 51.6455V21.7578Z" fill="url(#paint0_linear_1152_115027)"/></g></g><defs><filter id="filter0_d_1152_115027" x="0.300112" y="3.95898" width="71.3529" height="55.2377" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB"><feFlood flood-opacity="0" result="BackgroundImageFix"/><feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/><feOffset dy="2.74286"/><feGaussianBlur stdDeviation="1.37143"/><feColorMatrix type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.25 0"/><feBlend mode="normal" in2="BackgroundImageFix" result="effect1_dropShadow_1152_115027"/><feBlend mode="normal" in="SourceGraphic" in2="effect1_dropShadow_1152_115027" result="shape"/></filter><filter id="filter1_d_1152_115027" x="-2.44275" y="14.2076" width="76.8386" height="60.7234" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB"><feFlood flood-opacity="0" result="BackgroundImageFix"/><feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/><feOffset/><feGaussianBlur stdDeviation="2.74286"/><feColorMatrix type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.25 0"/><feBlend mode="overlay" in2="BackgroundImageFix" result="effect1_dropShadow_1152_115027"/><feBlend mode="normal" in="SourceGraphic" in2="effect1_dropShadow_1152_115027" result="shape"/></filter><filter id="filter2_d_1152_115027" x="42.858" y="-1.52868" width="31.5417" height="76.4597" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB"><feFlood flood-opacity="0" result="BackgroundImageFix"/><feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/><feOffset/><feGaussianBlur stdDeviation="2.74286"/><feColorMatrix type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.25 0"/><feBlend mode="overlay" in2="BackgroundImageFix" result="effect1_dropShadow_1152_115027"/><feBlend mode="normal" in="SourceGraphic" in2="effect1_dropShadow_1152_115027" result="shape"/></filter><linearGradient id="paint0_linear_1152_115027" x1="35.9573" y1="3.95703" x2="35.9573" y2="69.4462" gradientUnits="userSpaceOnUse"><stop stop-color="white"/><stop offset="1" stop-color="white" stop-opacity="0"/></linearGradient></defs></svg>`,
		warning: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="m40-120 440-760 440 760H40Zm138-80h604L480-720 178-200Zm302-40q17 0 28.5-11.5T520-280q0-17-11.5-28.5T480-320q-17 0-28.5 11.5T440-280q0 17 11.5 28.5T480-240Zm-40-120h80v-200h-80v200Zm40-100Z"/></svg>`,
		wbIncandescent: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M440-80v-120h80v120h-80ZM80-440v-80h120v80H80Zm680 0v-80h120v80H760Zm-40 276-84-84 56-56 84 84-56 56Zm-480 0-56-56 84-84 56 56-84 84Zm98.5-174.5Q280-397 280-480q0-48 21.5-89.5T360-640v-200h240v200q37 29 58.5 70.5T680-480q0 83-58.5 141.5T480-280q-83 0-141.5-58.5ZM440-676q10-2 20-3t20-1q10 0 20 1t20 3v-84h-80v84Zm40 316q50 0 85-35t35-85q0-50-35-85t-85-35q-50 0-85 35t-35 85q0 50 35 85t85 35Zm0-120Z"/></svg>`,
		webAsset: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M160-160q-33 0-56.5-23.5T80-240v-480q0-33 23.5-56.5T160-800h640q33 0 56.5 23.5T880-720v480q0 33-23.5 56.5T800-160H160Zm0-80h640v-400H160v400Z"/></svg>`,
		webTraffic: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M80-480v-80h120v80H80Zm136 222-56-58 84-84 58 56-86 86Zm28-382-84-84 56-58 86 86-58 56Zm476 480L530-350l-50 150-120-400 400 120-148 52 188 188-80 80ZM400-720v-120h80v120h-80Zm236 80-58-56 86-86 56 56-84 86Z"/></svg>`,
		west: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M360-200 80-480l280-280 56 56-183 184h647v80H233l184 184-57 56Z"/></svg>`,
		widget: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M666-440 440-666l226-226 226 226-226 226Zm-546-80v-320h320v320H120Zm400 400v-320h320v320H520Zm-400 0v-320h320v320H120Zm80-480h160v-160H200v160Zm467 48 113-113-113-113-113 113 113 113Zm-67 352h160v-160H600v160Zm-400 0h160v-160H200v160Zm160-400Zm194-65ZM360-360Zm240 0Z"/></svg>`,
		windows: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48.746779 48.746779"><rect fill="#0078d4" height="23.105" width="23.105" x="0" y="0"/><rect fill="#0078d4" height="23.105" width="23.105" x="25.639997" y="0"/><rect fill="#0078d4" height="23.105" width="23.105" x="0" y="25.642"/><rect fill="#0078d4" height="23.105" width="23.105" x="25.639997" y="25.642"/></svg>`,
		alertCircle: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M9 6V9M9 12H9.0075M16.5 9C16.5 13.1421 13.1421 16.5 9 16.5C4.85786 16.5 1.5 13.1421 1.5 9C1.5 4.85786 4.85786 1.5 9 1.5C13.1421 1.5 16.5 4.85786 16.5 9Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		alignBottom: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M2.25 15.75H15.75M9 2.25V12.75M9 12.75L14.25 7.5M9 12.75L3.75 7.5"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		alignHorizontalAlt: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M9 2.25V15.75M16.5 9H11.625M11.625 9L14.625 12M11.625 9L14.625 6M1.5 9H6.375M6.375 9L3.375 12M6.375 9L3.375 6"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		alignHorizontal: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M12 7.5C12.6989 7.5 13.0484 7.5 13.3241 7.38582C13.6916 7.23358 13.9836 6.94157 14.1359 6.57403C14.25 6.29837 14.25 5.94891 14.25 5.25C14.25 4.55109 14.25 4.20164 14.1359 3.92597C13.9836 3.55844 13.6916 3.26642 13.3241 3.11418C13.0484 3 12.6989 3 12 3H6C5.30109 3 4.95164 3 4.67597 3.11418C4.30844 3.26642 4.01642 3.55844 3.86418 3.92597C3.75 4.20164 3.75 4.55109 3.75 5.25C3.75 5.94891 3.75 6.29837 3.86418 6.57403C4.01642 6.94157 4.30844 7.23358 4.67597 7.38582C4.95164 7.5 5.30109 7.5 6 7.5H12Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
  <path
    d="M13.5 15C14.1989 15 14.5483 15 14.824 14.8858C15.1915 14.7336 15.4836 14.4415 15.6358 14.074C15.75 13.7983 15.75 13.4489 15.75 12.75C15.75 12.0511 15.75 11.7017 15.6358 11.426C15.4836 11.0585 15.1915 10.7664 14.824 10.6141C14.5483 10.5 14.1989 10.5 13.5 10.5H4.5C3.80109 10.5 3.45164 10.5 3.17597 10.6141C2.80844 10.7664 2.51642 11.0585 2.36418 11.426C2.25 11.7017 2.25 12.0511 2.25 12.75C2.25 13.4489 2.25 13.7983 2.36418 14.074C2.51642 14.4415 2.80844 14.7336 3.17597 14.8858C3.45164 15 3.80109 15 4.5 15H13.5Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		alignVerticalAlt: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M2.25 9H15.75M9 1.5V6.375M9 6.375L12 3.375M9 6.375L6 3.375M9 16.5V11.625M9 11.625L12 14.625M9 11.625L6 14.625"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		alignVertical: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M7.5 13.5V4.5C7.5 3.80109 7.5 3.45164 7.38582 3.17597C7.23358 2.80844 6.94157 2.51642 6.57403 2.36418C6.29837 2.25 5.94891 2.25 5.25 2.25C4.55109 2.25 4.20164 2.25 3.92597 2.36418C3.55844 2.51642 3.26642 2.80844 3.11418 3.17597C3 3.45164 3 3.80109 3 4.5V13.5C3 14.1989 3 14.5483 3.11418 14.824C3.26642 15.1915 3.55844 15.4836 3.92597 15.6358C4.20164 15.75 4.55109 15.75 5.25 15.75C5.94891 15.75 6.29837 15.75 6.57403 15.6358C6.94157 15.4836 7.23358 15.1915 7.38582 14.824C7.5 14.5483 7.5 14.1989 7.5 13.5Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
  <path
    d="M15 12V6C15 5.30109 15 4.95164 14.8858 4.67597C14.7336 4.30844 14.4415 4.01642 14.074 3.86418C13.7983 3.75 13.4489 3.75 12.75 3.75C12.0511 3.75 11.7017 3.75 11.426 3.86418C11.0585 4.01642 10.7664 4.30844 10.6141 4.67597C10.5 4.95164 10.5 5.30109 10.5 6V12C10.5 12.6989 10.5 13.0483 10.6141 13.324C10.7664 13.6915 11.0585 13.9836 11.426 14.1358C11.7017 14.25 12.0511 14.25 12.75 14.25C13.4489 14.25 13.7983 14.25 14.074 14.1358C14.4415 13.9836 14.7336 13.6915 14.8858 13.324C15 13.0483 15 12.6989 15 12Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		alignVerticalCenter: Q`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -960 960 960" fill="currentColor"><path d="M280-120v-320H80v-80h200v-320h120v320h160v-200h120v200h200v80H680v200H560v-200H400v320H280Z"/></svg>`,
		annotation: Q`
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M3 7.8C3 6.11984 3 5.27976 3.32698 4.63803C3.6146 4.07354 4.07354 3.6146 4.63803 3.32698C5.27976 3 6.11984 3 7.8 3H16.2C17.8802 3 18.7202 3 19.362 3.32698C19.9265 3.6146 20.3854 4.07354 20.673 4.63803C21 5.27976 21 6.11984 21 7.8V13.5C21 14.8978 21 15.5967 20.7716 16.1481C20.4672 16.8831 19.8831 17.4672 19.1481 17.7716C18.5967 18 17.8978 18 16.5 18C16.0114 18 15.7671 18 15.5405 18.0535C15.2383 18.1248 14.9569 18.2656 14.7185 18.4645C14.5397 18.6137 14.3931 18.8091 14.1 19.2L12.64 21.1467C12.4229 21.4362 12.3143 21.5809 12.1812 21.6327C12.0647 21.678 11.9353 21.678 11.8188 21.6327C11.6857 21.5809 11.5771 21.4362 11.36 21.1467L9.9 19.2C9.60685 18.8091 9.46028 18.6137 9.2815 18.4645C9.04312 18.2656 8.76169 18.1248 8.45951 18.0535C8.23287 18 7.98858 18 7.5 18C6.10218 18 5.40326 18 4.85195 17.7716C4.11687 17.4672 3.53284 16.8831 3.22836 16.1481C3 15.5967 3 14.8978 3 13.5V7.8Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>`,
		annotationX: Q`
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M9.5 8L14.5 13M14.5 8L9.5 13M9.9 19.2L11.36 21.1467C11.5771 21.4362 11.6857 21.5809 11.8188 21.6327C11.9353 21.678 12.0647 21.678 12.1812 21.6327C12.3143 21.5809 12.4229 21.4362 12.64 21.1467L14.1 19.2C14.3931 18.8091 14.5397 18.6137 14.7185 18.4645C14.9569 18.2656 15.2383 18.1248 15.5405 18.0535C15.7671 18 16.0114 18 16.5 18C17.8978 18 18.5967 18 19.1481 17.7716C19.8831 17.4672 20.4672 16.8831 20.7716 16.1481C21 15.5967 21 14.8978 21 13.5V7.8C21 6.11984 21 5.27976 20.673 4.63803C20.3854 4.07354 19.9265 3.6146 19.362 3.32698C18.7202 3 17.8802 3 16.2 3H7.8C6.11984 3 5.27976 3 4.63803 3.32698C4.07354 3.6146 3.6146 4.07354 3.32698 4.63803C3 5.27976 3 6.11984 3 7.8V13.5C3 14.8978 3 15.5967 3.22836 16.1481C3.53284 16.8831 4.11687 17.4672 4.85195 17.7716C5.40326 18 6.10218 18 7.5 18C7.98858 18 8.23287 18 8.45951 18.0535C8.76169 18.1248 9.04312 18.2656 9.2815 18.4645C9.46028 18.6137 9.60685 18.8091 9.9 19.2Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>`,
		arrowDownLeft: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M12.75 5.25L5.25 12.75M5.25 12.75H12.75M5.25 12.75V5.25"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		arrowDownRight: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M5.25 5.25L12.75 12.75M12.75 12.75V5.25M12.75 12.75H5.25"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		arrowDown: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M9 3.75V14.25M9 14.25L14.25 9M9 14.25L3.75 9"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		arrowLeft: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M15 9H3M3 9L7.5 13.5M3 9L7.5 4.5"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		arrowRight: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M3 9H15M15 9L10.5 4.5M15 9L10.5 13.5"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		arrowUpLeft: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M12.75 12.75L5.25 5.25M5.25 5.25V12.75M5.25 5.25H12.75"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		arrowUpRight: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M5.25 12.75L12.75 5.25M12.75 5.25H5.25M12.75 5.25V12.75"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		atSign: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <g clip-path="url(#clip0_858_19587)">
    <path
      d="M12 5.99999V9.75C12 10.3467 12.2371 10.919 12.659 11.341C13.081 11.7629 13.6532 12 14.25 12C14.8467 12 15.419 11.7629 15.841 11.341C16.2629 10.919 16.5 10.3467 16.5 9.75V9C16.4999 7.30726 15.9272 5.66435 14.875 4.33839C13.8227 3.01243 12.3529 2.08141 10.7045 1.69672C9.05604 1.31202 7.32596 1.49627 5.79552 2.21952C4.26508 2.94276 3.0243 4.16246 2.27495 5.68028C1.52559 7.19811 1.31173 8.92477 1.66813 10.5796C2.02453 12.2344 2.93024 13.7199 4.23798 14.7947C5.54572 15.8695 7.17858 16.4702 8.87109 16.4993C10.5635 16.5284 12.2161 15.9842 13.56 14.955M12 9C12 10.6568 10.6568 12 9.00001 12C7.34316 12 6.00001 10.6568 6.00001 9C6.00001 7.34314 7.34316 5.99999 9.00001 5.99999C10.6568 5.99999 12 7.34314 12 9Z"
      stroke="currentColor"
      stroke-width="1.5"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
  </g>
  <defs>
    <clipPath id="clip0_858_19587">
      <rect width="18" height="18" fill="white" />
    </clipPath>
  </defs>
</svg>`,
		atom: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M8.99963 9.00007H9.00713M11.6513 11.6518C8.1366 15.1665 4.10014 16.8285 2.63567 15.364C1.17121 13.8996 2.83327 9.86317 6.34799 6.34846C9.86273 2.83374 13.8992 1.17168 15.3636 2.63614C16.8281 4.10061 15.166 8.13705 11.6513 11.6518ZM11.6513 6.34832C15.166 9.86302 16.8281 13.8995 15.3636 15.3639C13.8991 16.8284 9.86265 15.1663 6.34796 11.6516C2.83324 8.1369 1.17118 4.10047 2.63564 2.63601C4.10011 1.17154 8.13653 2.8336 11.6513 6.34832ZM9.37463 9.00007C9.37463 9.20722 9.20678 9.37507 8.99963 9.37507C8.79255 9.37507 8.62463 9.20722 8.62463 9.00007C8.62463 8.793 8.79255 8.62507 8.99963 8.62507C9.20678 8.62507 9.37463 8.793 9.37463 9.00007Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		autoMode: Q`
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"><g><rect fill="none" height="24" width="24"/></g><g><g><path d="M19.03,3.56c-1.67-1.39-3.74-2.3-6.03-2.51v2.01c1.73,0.19,3.31,0.88,4.61,1.92L19.03,3.56z"/><path d="M11,3.06V1.05C8.71,1.25,6.64,2.17,4.97,3.56l1.42,1.42C7.69,3.94,9.27,3.25,11,3.06z"/><path d="M4.98,6.39L3.56,4.97C2.17,6.64,1.26,8.71,1.05,11h2.01C3.25,9.27,3.94,7.69,4.98,6.39z"/><path d="M20.94,11h2.01c-0.21-2.29-1.12-4.36-2.51-6.03l-1.42,1.42C20.06,7.69,20.75,9.27,20.94,11z"/><polygon points="7,12 10.44,13.56 12,17 13.56,13.56 17,12 13.56,10.44 12,7 10.44,10.44"/><path d="M12,21c-3.11,0-5.85-1.59-7.46-4H7v-2H1v6h2v-2.7c1.99,2.84,5.27,4.7,9,4.7c4.87,0,9-3.17,10.44-7.56l-1.96-0.45 C19.25,18.48,15.92,21,12,21z"/></g></g></svg>
  `,
		bell: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M11.2498 14.25C11.2498 15.4927 10.2424 16.5 8.99977 16.5C7.75717 16.5 6.74979 15.4927 6.74979 14.25M10.3471 4.67892C10.6738 4.34148 10.8748 3.88172 10.8748 3.375C10.8748 2.33947 10.0353 1.5 8.99977 1.5C7.96425 1.5 7.12479 2.33947 7.12479 3.375C7.12479 3.88172 7.32579 4.34148 7.6524 4.67892M13.4998 8.4C13.4998 7.36565 13.0257 6.37367 12.1818 5.64228C11.3378 4.91089 10.1932 4.5 8.99977 4.5C7.8063 4.5 6.66172 4.91089 5.81781 5.64228C4.97389 6.37367 4.49979 7.36565 4.49979 8.4C4.49979 10.1113 4.07538 11.3629 3.54583 12.2585C2.94229 13.2792 2.64052 13.7896 2.65244 13.9114C2.66607 14.0509 2.69118 14.095 2.80429 14.1777C2.90316 14.25 3.39992 14.25 4.39343 14.25H13.6061C14.5996 14.25 15.0964 14.25 15.1953 14.1777C15.3084 14.095 15.3335 14.0509 15.3472 13.9114C15.359 13.7896 15.0573 13.2792 14.4538 12.2585C13.9242 11.3629 13.4998 10.1113 13.4998 8.4Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		bookOpen: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M9 15.75L8.92493 15.6374C8.40398 14.856 8.1435 14.4652 7.79933 14.1823C7.49465 13.9319 7.14357 13.744 6.76621 13.6294C6.33994 13.5 5.87033 13.5 4.93112 13.5H3.9C3.05992 13.5 2.63988 13.5 2.31902 13.3365C2.03677 13.1927 1.8073 12.9632 1.66349 12.681C1.5 12.3602 1.5 11.9401 1.5 11.1V4.65C1.5 3.80992 1.5 3.38988 1.66349 3.06902C1.8073 2.78677 2.03677 2.5573 2.31902 2.41349C2.63988 2.25 3.05992 2.25 3.9 2.25H4.2C5.88016 2.25 6.72023 2.25 7.36197 2.57698C7.92645 2.8646 8.38538 3.32354 8.673 3.88803C9 4.52976 9 5.36984 9 7.05M9 15.75V7.05M9 15.75L9.07507 15.6374C9.59602 14.856 9.8565 14.4652 10.2007 14.1823C10.5053 13.9319 10.8564 13.744 11.2338 13.6294C11.66 13.5 12.1297 13.5 13.0689 13.5H14.1C14.9401 13.5 15.3602 13.5 15.681 13.3365C15.9632 13.1927 16.1927 12.9632 16.3365 12.681C16.5 12.3602 16.5 11.9401 16.5 11.1V4.65C16.5 3.80992 16.5 3.38988 16.3365 3.06902C16.1927 2.78677 15.9632 2.5573 15.681 2.41349C15.3602 2.25 14.9401 2.25 14.1 2.25H13.8C12.1198 2.25 11.2798 2.25 10.638 2.57698C10.0736 2.8646 9.61462 3.32354 9.327 3.88803C9 4.52976 9 5.36984 9 7.05"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		calendarPlus: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M15.75 6H2.25M12 1.5V3.75M6 1.5V3.75M9 13.5V9M6.75 11.25H11.25M5.85 16.5H12.15C13.4102 16.5 14.0401 16.5 14.5215 16.2548C14.9449 16.0391 15.2891 15.6949 15.5048 15.2715C15.75 14.7901 15.75 14.1602 15.75 12.9V6.6C15.75 5.33988 15.75 4.70982 15.5048 4.22852C15.2891 3.80516 14.9449 3.46095 14.5215 3.24524C14.0401 3 13.4102 3 12.15 3H5.85C4.58988 3 3.95982 3 3.47852 3.24524C3.05516 3.46095 2.71095 3.80516 2.49524 4.22852C2.25 4.70982 2.25 5.33988 2.25 6.6V12.9C2.25 14.1602 2.25 14.7901 2.49524 15.2715C2.71095 15.6949 3.05516 16.0391 3.47852 16.2548C3.95982 16.5 4.58988 16.5 5.85 16.5Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		calendar: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M15.75 7.5H2.25M12 1.5V4.5M6 1.5V4.5M5.85 16.5H12.15C13.4102 16.5 14.0401 16.5 14.5215 16.2548C14.9449 16.0391 15.2891 15.6949 15.5048 15.2715C15.75 14.7901 15.75 14.1602 15.75 12.9V6.6C15.75 5.33988 15.75 4.70982 15.5048 4.22852C15.2891 3.80516 14.9449 3.46095 14.5215 3.24524C14.0401 3 13.4102 3 12.15 3H5.85C4.58988 3 3.95982 3 3.47852 3.24524C3.05516 3.46095 2.71095 3.80516 2.49524 4.22852C2.25 4.70982 2.25 5.33988 2.25 6.6V12.9C2.25 14.1602 2.25 14.7901 2.49524 15.2715C2.71095 15.6949 3.05516 16.0391 3.47852 16.2548C3.95982 16.5 4.58988 16.5 5.85 16.5Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		caretDown: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path d="M9 12L4 7H14L9 12Z" fill="currentColor" />
</svg>`,
		caretLeft: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path d="M7 9L12 4V14L7 9Z" fill="currentColor" />
</svg>`,
		caretRight: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path d="M12 9L7 4V14L12 9Z" fill="currentColor" />
</svg>`,
		caretUp: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path d="M9 7L4 12H14L9 7Z" fill="currentColor" />
</svg>`,
		category: Q`<svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M4.875 8.25L9 1.5L13.125 8.25H4.875ZM13.125 16.5C12.1875 16.5 11.3906 16.1719 10.7344 15.5156C10.0781 14.8594 9.75 14.0625 9.75 13.125C9.75 12.1875 10.0781 11.3906 10.7344 10.7344C11.3906 10.0781 12.1875 9.75 13.125 9.75C14.0625 9.75 14.8594 10.0781 15.5156 10.7344C16.1719 11.3906 16.5 12.1875 16.5 13.125C16.5 14.0625 16.1719 14.8594 15.5156 15.5156C14.8594 16.1719 14.0625 16.5 13.125 16.5ZM2.25 16.125V10.125H8.25V16.125H2.25ZM13.125 15C13.65 15 14.0938 14.8188 14.4563 14.4563C14.8188 14.0938 15 13.65 15 13.125C15 12.6 14.8188 12.1563 14.4563 11.7938C14.0938 11.4313 13.65 11.25 13.125 11.25C12.6 11.25 12.1563 11.4313 11.7938 11.7938C11.4313 12.1563 11.25 12.6 11.25 13.125C11.25 13.65 11.4313 14.0938 11.7938 14.4563C12.1563 14.8188 12.6 15 13.125 15ZM3.75 14.625H6.75V11.625H3.75V14.625ZM7.5375 6.75H10.4625L9 4.3875L7.5375 6.75Z" fill="currentColor"/>
</svg>`,
		checkDone: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <g clip-path="url(#clip0_858_19549)">
    <path
      d="M12 6V3.9C12 3.05992 12 2.63988 11.8365 2.31902C11.6927 2.03677 11.4632 1.8073 11.181 1.66349C10.8601 1.5 10.4401 1.5 9.6 1.5H3.9C3.05992 1.5 2.63988 1.5 2.31902 1.66349C2.03677 1.8073 1.8073 2.03677 1.66349 2.31902C1.5 2.63988 1.5 3.05992 1.5 3.9V9.6C1.5 10.4401 1.5 10.8601 1.66349 11.181C1.8073 11.4632 2.03677 11.6927 2.31902 11.8365C2.63988 12 3.05992 12 3.9 12H6M9 11.25L10.5 12.75L13.875 9.375M8.4 16.5H14.1C14.9401 16.5 15.3602 16.5 15.681 16.3365C15.9632 16.1927 16.1927 15.9632 16.3365 15.681C16.5 15.3602 16.5 14.9401 16.5 14.1V8.4C16.5 7.55992 16.5 7.13988 16.3365 6.81902C16.1927 6.53677 15.9632 6.3073 15.681 6.16349C15.3602 6 14.9401 6 14.1 6H8.4C7.55992 6 7.13988 6 6.81902 6.16349C6.53677 6.3073 6.3073 6.53677 6.16349 6.81902C6 7.13988 6 7.55992 6 8.4V14.1C6 14.9401 6 15.3602 6.16349 15.681C6.3073 15.9632 6.53677 16.1927 6.81902 16.3365C7.13988 16.5 7.55992 16.5 8.4 16.5Z"
      stroke="currentColor"
      stroke-width="1.5"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
  </g>
  <defs>
    <clipPath id="clip0_858_19549">
      <rect width="18" height="18" fill="white" />
    </clipPath>
  </defs>
</svg>`,
		checkSquare: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M5.625 9L7.875 11.25L12.375 6.75M5.85 15.75H12.15C13.4102 15.75 14.0401 15.75 14.5215 15.5048C14.9449 15.2891 15.2891 14.9449 15.5048 14.5215C15.75 14.0401 15.75 13.4102 15.75 12.15V5.85C15.75 4.58988 15.75 3.95982 15.5048 3.47852C15.2891 3.05516 14.9449 2.71095 14.5215 2.49524C14.0401 2.25 13.4102 2.25 12.15 2.25H5.85C4.58988 2.25 3.95982 2.25 3.47852 2.49524C3.05516 2.71095 2.71095 3.05516 2.49524 3.47852C2.25 3.95982 2.25 4.58988 2.25 5.85V12.15C2.25 13.4102 2.25 14.0401 2.49524 14.5215C2.71095 14.9449 3.05516 15.2891 3.47852 15.5048C3.95982 15.75 4.58988 15.75 5.85 15.75Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		chevronSelectorHorizontal: Q`
<svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M6.75 5.25L3 9L6.75 12.75M11.25 5.25L15 9L11.25 12.75" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
</svg>`,
		chevronSelectorVertical: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M5.25 11.25L9 15L12.75 11.25M5.25 6.75L9 3L12.75 6.75"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		circle: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <g clip-path="url(#clip0_907_8660)">
    <path
      d="M9 16.5C13.1421 16.5 16.5 13.1421 16.5 9C16.5 4.85786 13.1421 1.5 9 1.5C4.85786 1.5 1.5 4.85786 1.5 9C1.5 13.1421 4.85786 16.5 9 16.5Z"
      stroke="currentColor"
      stroke-width="1.5"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
  </g>
  <defs>
    <clipPath id="clip0_907_8660">
      <rect width="18" height="18" fill="white" />
    </clipPath>
  </defs>
</svg>`,
		clickAlt: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <g clip-path="url(#clip0_1705_14570)">
    <path
      d="M6.75 2.625V1.5M3.79549 3.79549L3 3M3.79549 9.75L3 10.5455M9.75 3.79549L10.5455 3M2.625 6.75H1.5M6.375 6.375L9.45833 15.9583L11.625 13.7917L14.3333 16.5L16.5 14.3333L13.7917 11.625L15.9583 9.45833L6.375 6.375Z"
      stroke="currentColor"
      stroke-width="1.5"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
  </g>
  <defs>
    <clipPath id="clip0_1705_14570">
      <rect width="18" height="18" fill="white" />
    </clipPath>
  </defs>
</svg>`,
		click: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <g clip-path="url(#clip0_1705_14569)">
    <path
      d="M6.75 2.625V1.5M3.79549 3.79549L3 3M3.79549 9.75L3 10.5455M9.75 3.79549L10.5455 3M2.625 6.75H1.5M11.8984 12.1422L10.0295 15.6127C9.81607 16.0093 9.70927 16.2075 9.58087 16.2577C9.46942 16.3012 9.34388 16.2889 9.243 16.2246C9.12668 16.1505 9.06045 15.9353 8.92785 15.505L6.33389 7.08394C6.2256 6.73238 6.17145 6.5566 6.21502 6.43787C6.25297 6.33446 6.33446 6.25297 6.43787 6.21503C6.5566 6.17146 6.73237 6.2256 7.08394 6.33389L15.5049 8.92785C15.9353 9.06045 16.1504 9.12675 16.2245 9.243C16.2889 9.34395 16.3011 9.46942 16.2576 9.58087C16.2075 9.70935 16.0092 9.81607 15.6127 10.0296L12.1422 11.8984C12.0832 11.9301 12.0538 11.9459 12.028 11.9663C12.0051 11.9844 11.9844 12.0051 11.9663 12.028C11.9459 12.0538 11.9301 12.0832 11.8984 12.1422Z"
      stroke="currentColor"
      stroke-width="1.5"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
  </g>
  <defs>
    <clipPath id="clip0_1705_14569">
      <rect width="18" height="18" fill="white" />
    </clipPath>
  </defs>
</svg>`,
		codeAlt: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M12.75 12.75L16.5 9L12.75 5.25M5.25 5.25L1.5 9L5.25 12.75M10.5 2.25L7.5 15.75"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		codeBrowser: Q`
<svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
  <path d="M22 9H2M14 17.5L16.5 15L14 12.5M10 12.5L7.5 15L10 17.5M2 7.8L2 16.2C2 17.8802 2 18.7202 2.32698 19.362C2.6146 19.9265 3.07354 20.3854 3.63803 20.673C4.27976 21 5.11984 21 6.8 21H17.2C18.8802 21 19.7202 21 20.362 20.673C20.9265 20.3854 21.3854 19.9265 21.673 19.362C22 18.7202 22 17.8802 22 16.2V7.8C22 6.11984 22 5.27977 21.673 4.63803C21.3854 4.07354 20.9265 3.6146 20.362 3.32698C19.7202 3 18.8802 3 17.2 3L6.8 3C5.11984 3 4.27976 3 3.63803 3.32698C3.07354 3.6146 2.6146 4.07354 2.32698 4.63803C2 5.27976 2 6.11984 2 7.8Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
</svg>`,
		coinsHand: Q`
  <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
<g clip-path="url(#clip0_2290_25891)">
<path d="M10.1471 6.2639C9.71783 6.56996 9.19245 6.75 8.625 6.75C7.17525 6.75 6 5.57475 6 4.125C6 2.67525 7.17525 1.5 8.625 1.5C9.56475 1.5 10.3891 1.99381 10.8529 2.7361M4.5 15.0654H6.45772C6.71297 15.0654 6.96666 15.0958 7.21411 15.1564L9.28267 15.6592C9.73147 15.7685 10.1991 15.7791 10.6526 15.6911L12.9398 15.2461C13.5439 15.1284 14.0997 14.8391 14.5352 14.4154L16.1534 12.8413C16.6155 12.3926 16.6155 11.6643 16.1534 11.2148C15.7374 10.8101 15.0785 10.7645 14.6078 11.1077L12.722 12.4837C12.4519 12.6811 12.1232 12.7874 11.7853 12.7874H9.96412H11.1233C11.7767 12.7874 12.3059 12.2725 12.3059 11.6369V11.4068C12.3059 10.8791 11.9367 10.419 11.4106 10.2914L9.62145 9.85627C9.3303 9.7857 9.0321 9.75 8.73232 9.75C8.00872 9.75 6.69892 10.3491 6.69892 10.3491L4.5 11.2687M15 4.875C15 6.32475 13.8247 7.5 12.375 7.5C10.9253 7.5 9.75 6.32475 9.75 4.875C9.75 3.42525 10.9253 2.25 12.375 2.25C13.8247 2.25 15 3.42525 15 4.875ZM1.5 10.95V15.3C1.5 15.7201 1.5 15.9301 1.58174 16.0905C1.65365 16.2317 1.76839 16.3463 1.90951 16.4183C2.06994 16.5 2.27996 16.5 2.7 16.5H3.3C3.72004 16.5 3.93006 16.5 4.09049 16.4183C4.23161 16.3463 4.34635 16.2317 4.41826 16.0905C4.5 15.9301 4.5 15.7201 4.5 15.3V10.95C4.5 10.5299 4.5 10.3199 4.41826 10.1595C4.34635 10.0183 4.23161 9.90368 4.09049 9.83175C3.93006 9.75 3.72004 9.75 3.3 9.75H2.7C2.27996 9.75 2.06994 9.75 1.90951 9.83175C1.76839 9.90368 1.65365 10.0183 1.58174 10.1595C1.5 10.3199 1.5 10.5299 1.5 10.95Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
</g>
<defs>
<clipPath id="clip0_2290_25891">
<rect width="18" height="18" fill="white"/>
</clipPath>
</defs>
</svg>`,
		columnsAlt: Q`
  <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M5.1 2.25H4.65C3.80992 2.25 3.38988 2.25 3.06902 2.41349C2.78677 2.5573 2.5573 2.78677 2.41349 3.06902C2.25 3.38988 2.25 3.80992 2.25 4.65V13.35C2.25 14.1901 2.25 14.6102 2.41349 14.931C2.5573 15.2132 2.78677 15.4427 3.06902 15.5865C3.38988 15.75 3.80992 15.75 4.65 15.75H5.1C5.94008 15.75 6.36012 15.75 6.68098 15.5865C6.96323 15.4427 7.1927 15.2132 7.33651 14.931C7.5 14.6102 7.5 14.1901 7.5 13.35V4.65C7.5 3.80992 7.5 3.38988 7.33651 3.06902C7.1927 2.78677 6.96323 2.5573 6.68098 2.41349C6.36012 2.25 5.94008 2.25 5.1 2.25Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
<path d="M13.35 2.25H12.9C12.0599 2.25 11.6399 2.25 11.319 2.41349C11.0368 2.5573 10.8073 2.78677 10.6635 3.06902C10.5 3.38988 10.5 3.80992 10.5 4.65V13.35C10.5 14.1901 10.5 14.6102 10.6635 14.931C10.8073 15.2132 11.0368 15.4427 11.319 15.5865C11.6399 15.75 12.0599 15.75 12.9 15.75H13.35C14.1901 15.75 14.6102 15.75 14.931 15.5865C15.2132 15.4427 15.4427 15.2132 15.5865 14.931C15.75 14.6102 15.75 14.1901 15.75 13.35V4.65C15.75 3.80992 15.75 3.38988 15.5865 3.06902C15.4427 2.78677 15.2132 2.5573 14.931 2.41349C14.6102 2.25 14.1901 2.25 13.35 2.25Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
</svg>`,
		columns: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M9 2.25V15.75M5.85 2.25H12.15C13.4102 2.25 14.0401 2.25 14.5215 2.49524C14.9449 2.71095 15.2891 3.05516 15.5048 3.47852C15.75 3.95982 15.75 4.58988 15.75 5.85V12.15C15.75 13.4102 15.75 14.0401 15.5048 14.5215C15.2891 14.9449 14.9449 15.2891 14.5215 15.5048C14.0401 15.75 13.4102 15.75 12.15 15.75H5.85C4.58988 15.75 3.95982 15.75 3.47852 15.5048C3.05516 15.2891 2.71095 14.9449 2.49524 14.5215C2.25 14.0401 2.25 13.4102 2.25 12.15V5.85C2.25 4.58988 2.25 3.95982 2.49524 3.47852C2.71095 3.05516 3.05516 2.71095 3.47852 2.49524C3.95982 2.25 4.58988 2.25 5.85 2.25Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		command: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M6.75 6.75V4.5C6.75 3.25736 5.74264 2.25 4.5 2.25C3.25736 2.25 2.25 3.25736 2.25 4.5C2.25 5.74264 3.25736 6.75 4.5 6.75H6.75ZM6.75 6.75V11.25M6.75 6.75H11.25M6.75 11.25V13.5C6.75 14.7427 5.74264 15.75 4.5 15.75C3.25736 15.75 2.25 14.7427 2.25 13.5C2.25 12.2573 3.25736 11.25 4.5 11.25H6.75ZM6.75 11.25H11.25M11.25 6.75V11.25M11.25 6.75V4.5C11.25 3.25736 12.2573 2.25 13.5 2.25C14.7427 2.25 15.75 3.25736 15.75 4.5C15.75 5.74264 14.7427 6.75 13.5 6.75H11.25ZM11.25 11.25H13.5C14.7427 11.25 15.75 12.2573 15.75 13.5C15.75 14.7427 14.7427 15.75 13.5 15.75C12.2573 15.75 11.25 14.7427 11.25 13.5V11.25Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		cookie: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M9 16.5C7.9625 16.5 6.9875 16.3031 6.075 15.9094C5.1625 15.5156 4.36875 14.9813 3.69375 14.3063C3.01875 13.6313 2.48438 12.8375 2.09063 11.925C1.69688 11.0125 1.5 10.0375 1.5 9.00001C1.5 8.06251 1.68125 7.14376 2.04375 6.24376C2.40625 5.34376 2.9125 4.54064 3.5625 3.83439C4.2125 3.12814 4.99375 2.55939 5.90625 2.12814C6.81875 1.69689 7.81875 1.48126 8.90625 1.48126C9.16875 1.48126 9.4375 1.49376 9.7125 1.51876C9.9875 1.54376 10.2688 1.58751 10.5563 1.65001C10.4438 2.21251 10.4813 2.74376 10.6688 3.24376C10.8563 3.74376 11.1375 4.15939 11.5125 4.49064C11.8875 4.82189 12.3344 5.05001 12.8531 5.17501C13.3719 5.30001 13.9063 5.26876 14.4563 5.08126C14.1313 5.81876 14.1781 6.52501 14.5969 7.20001C15.0156 7.87501 15.6375 8.22501 16.4625 8.25001C16.475 8.38751 16.4844 8.51564 16.4906 8.63439C16.4969 8.75314 16.5 8.88126 16.5 9.01876C16.5 10.0438 16.3031 11.0094 15.9094 11.9156C15.5156 12.8219 14.9813 13.6156 14.3063 14.2969C13.6313 14.9781 12.8375 15.5156 11.925 15.9094C11.0125 16.3031 10.0375 16.5 9 16.5ZM7.875 7.50001C8.1875 7.50001 8.45313 7.39064 8.67188 7.17189C8.89063 6.95314 9 6.68751 9 6.37501C9 6.06251 8.89063 5.79689 8.67188 5.57814C8.45313 5.35939 8.1875 5.25001 7.875 5.25001C7.5625 5.25001 7.29688 5.35939 7.07812 5.57814C6.85938 5.79689 6.75 6.06251 6.75 6.37501C6.75 6.68751 6.85938 6.95314 7.07812 7.17189C7.29688 7.39064 7.5625 7.50001 7.875 7.50001ZM6.375 11.25C6.6875 11.25 6.95312 11.1406 7.17188 10.9219C7.39063 10.7031 7.5 10.4375 7.5 10.125C7.5 9.81251 7.39063 9.54689 7.17188 9.32814C6.95312 9.10939 6.6875 9.00001 6.375 9.00001C6.0625 9.00001 5.79688 9.10939 5.57812 9.32814C5.35938 9.54689 5.25 9.81251 5.25 10.125C5.25 10.4375 5.35938 10.7031 5.57812 10.9219C5.79688 11.1406 6.0625 11.25 6.375 11.25ZM11.25 12C11.4625 12 11.6406 11.9281 11.7844 11.7844C11.9281 11.6406 12 11.4625 12 11.25C12 11.0375 11.9281 10.8594 11.7844 10.7156C11.6406 10.5719 11.4625 10.5 11.25 10.5C11.0375 10.5 10.8594 10.5719 10.7156 10.7156C10.5719 10.8594 10.5 11.0375 10.5 11.25C10.5 11.4625 10.5719 11.6406 10.7156 11.7844C10.8594 11.9281 11.0375 12 11.25 12ZM9 15C10.525 15 11.8781 14.475 13.0594 13.425C14.2406 12.375 14.8875 11.0375 15 9.41251C14.375 9.13751 13.8844 8.76251 13.5281 8.28751C13.1719 7.81251 12.9313 7.28126 12.8063 6.69376C11.8438 6.55626 11.0188 6.14376 10.3313 5.45626C9.64375 4.76876 9.21875 3.94376 9.05625 2.98126C8.05625 2.95626 7.17813 3.13751 6.42188 3.52501C5.66563 3.91251 5.03438 4.40939 4.52813 5.01564C4.02188 5.62189 3.64062 6.28126 3.38438 6.99376C3.12813 7.70626 3 8.37501 3 9.00001C3 10.6625 3.58438 12.0781 4.75313 13.2469C5.92188 14.4156 7.3375 15 9 15Z"
    fill="currentColor"
  />
</svg>`,
		cornerDownRight: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M3 3V4.05C3 6.57023 3 7.83037 3.49047 8.79292C3.9219 9.63967 4.61031 10.3281 5.45705 10.7595C6.41965 11.25 7.67978 11.25 10.2 11.25H15M15 11.25L11.25 7.5M15 11.25L11.25 15"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		creditCard: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M16.5 7.5H1.5M1.5 6.15V11.85C1.5 12.6901 1.5 13.1102 1.66349 13.431C1.8073 13.7132 2.03677 13.9427 2.31901 14.0865C2.63988 14.25 3.05992 14.25 3.9 14.25H14.1C14.9401 14.25 15.3601 14.25 15.681 14.0865C15.9632 13.9427 16.1927 13.7132 16.3365 13.431C16.5 13.1102 16.5 12.6901 16.5 11.85V6.15C16.5 5.30993 16.5 4.88988 16.3365 4.56902C16.1927 4.28678 15.9632 4.0573 15.681 3.91349C15.3601 3.75 14.9401 3.75 14.1 3.75H3.9C3.05992 3.75 2.63988 3.75 2.31901 3.91349C2.03677 4.0573 1.8073 4.28677 1.66349 4.56902C1.5 4.88988 1.5 5.30992 1.5 6.15Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		cursorAlt: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M15.3791 8.08155C15.8418 7.90162 16.0732 7.81162 16.1381 7.68442C16.1944 7.57425 16.1927 7.44334 16.1336 7.33465C16.0653 7.20919 15.8317 7.12533 15.3644 6.9576L3.44716 2.6796C3.06489 2.54237 2.87375 2.47376 2.74879 2.51704C2.64016 2.55466 2.55478 2.64003 2.51716 2.74867C2.47388 2.87362 2.5425 3.06476 2.67972 3.44704L6.95769 15.3643C7.12542 15.8316 7.20928 16.0652 7.33474 16.1335C7.44343 16.1926 7.57426 16.1943 7.68451 16.138C7.81171 16.0731 7.90164 15.8417 8.08164 15.379L10.0292 10.3708C10.0645 10.2802 10.0821 10.2349 10.1093 10.1968C10.1335 10.1629 10.163 10.1334 10.1969 10.1092C10.235 10.082 10.2803 10.0644 10.3709 10.0291L15.3791 8.08155Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		cursorBox: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M15.75 7.125V5.85C15.75 4.58988 15.75 3.95982 15.5048 3.47852C15.2891 3.05516 14.9449 2.71095 14.5215 2.49524C14.0401 2.25 13.4102 2.25 12.15 2.25H5.85C4.58988 2.25 3.95982 2.25 3.47852 2.49524C3.05516 2.71095 2.71095 3.05516 2.49524 3.47852C2.25 3.95982 2.25 4.58988 2.25 5.85V12.15C2.25 13.4102 2.25 14.0401 2.49524 14.5215C2.71095 14.9449 3.05516 15.2891 3.47852 15.5048C3.95982 15.75 4.58988 15.75 5.85 15.75H7.125M13.0396 13.2835L11.7659 15.649C11.5577 16.0356 11.4536 16.229 11.3267 16.2794C11.2166 16.3232 11.0922 16.3123 10.9913 16.2501C10.8751 16.1784 10.8061 15.97 10.6681 15.5531L8.6253 9.38348C8.5044 9.01838 8.44395 8.8359 8.48745 8.71432C8.52525 8.6085 8.6085 8.52525 8.71432 8.48745C8.8359 8.44395 9.01838 8.5044 9.38348 8.6253L15.5531 10.6681C15.97 10.8062 16.1784 10.8751 16.2501 10.9913C16.3123 11.0923 16.3231 11.2166 16.2793 11.3268C16.2289 11.4536 16.0356 11.5577 15.6489 11.7659L13.2835 13.0396C13.2246 13.0714 13.1951 13.0873 13.1693 13.1076C13.1464 13.1258 13.1258 13.1464 13.1076 13.1693C13.0873 13.1951 13.0714 13.2246 13.0396 13.2835Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		cursor: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M9.74992 9.74992L14.2499 14.2499M13.473 8.0634L9.71497 9.33982C9.61222 9.3747 9.56085 9.39217 9.5181 9.4215C9.4803 9.44752 9.44752 9.4803 9.4215 9.5181C9.39217 9.56085 9.3747 9.61222 9.33982 9.71497L8.0634 13.473C7.90515 13.939 7.82595 14.172 7.7022 14.2425C7.59502 14.3035 7.46461 14.3079 7.35351 14.2542C7.2253 14.1922 7.13067 13.9651 6.94141 13.5108L2.81024 3.59484C2.63462 3.17332 2.54682 2.96255 2.58954 2.83069C2.6266 2.71629 2.71629 2.6266 2.83069 2.58954C2.96255 2.54682 3.17332 2.63462 3.59484 2.81024L13.5108 6.94141C13.9651 7.13067 14.1922 7.2253 14.2542 7.35351C14.3079 7.46461 14.3035 7.59502 14.2425 7.7022C14.172 7.82595 13.939 7.90515 13.473 8.0634Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		databaseUpload: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M9 6.76875C10.1125 6.76875 11.2312 6.60312 12.3562 6.27187C13.4812 5.94062 14.1125 5.60625 14.25 5.26875C14.1125 4.90625 13.4844 4.5625 12.3656 4.2375C11.2469 3.9125 10.125 3.75 9 3.75C7.8625 3.75 6.74687 3.90937 5.65312 4.22812C4.55937 4.54687 3.925 4.89375 3.75 5.26875C3.925 5.60625 4.55937 5.94062 5.65312 6.27187C6.74687 6.60312 7.8625 6.76875 9 6.76875ZM13.125 15.75H13.875V12.675L15.225 14.025L15.75 13.5L13.5 11.25L11.25 13.5L11.775 14.025L13.125 12.675V15.75ZM13.5 17.25C12.4625 17.25 11.5781 16.8844 10.8469 16.1531C10.1156 15.4219 9.75 14.5375 9.75 13.5C9.75 12.4625 10.1156 11.5781 10.8469 10.8469C11.5781 10.1156 12.4625 9.75 13.5 9.75C14.5375 9.75 15.4219 10.1156 16.1531 10.8469C16.8844 11.5781 17.25 12.4625 17.25 13.5C17.25 14.5375 16.8844 15.4219 16.1531 16.1531C15.4219 16.8844 14.5375 17.25 13.5 17.25ZM8.30625 14.2312C8.34375 14.5062 8.4 14.7687 8.475 15.0187C8.55 15.2687 8.64375 15.5125 8.75625 15.75C7.84375 15.7375 6.99375 15.65 6.20625 15.4875C5.41875 15.325 4.73125 15.1094 4.14375 14.8406C3.55625 14.5719 3.09375 14.2594 2.75625 13.9031C2.41875 13.5469 2.25 13.1625 2.25 12.75V5.25C2.25 4.425 2.90937 3.71875 4.22812 3.13125C5.54687 2.54375 7.1375 2.25 9 2.25C10.8625 2.25 12.4531 2.54375 13.7719 3.13125C15.0906 3.71875 15.75 4.425 15.75 5.25V8.75625C15.5125 8.64375 15.2687 8.55 15.0187 8.475C14.7687 8.4 14.5125 8.34375 14.25 8.30625V7.14375C13.6 7.50625 12.825 7.78125 11.925 7.96875C11.025 8.15625 10.05 8.25 9 8.25C7.9375 8.25 6.95625 8.15625 6.05625 7.96875C5.15625 7.78125 4.3875 7.50625 3.75 7.14375V9.0375C4.3875 9.625 5.20312 10.0156 6.19687 10.2094C7.19062 10.4031 8.125 10.5 9 10.5H9.20625C9.04375 10.725 8.90312 10.9625 8.78437 11.2125C8.66562 11.4625 8.5625 11.725 8.475 12C7.525 11.95 6.64375 11.8344 5.83125 11.6531C5.01875 11.4719 4.325 11.2187 3.75 10.8937V12.75C3.8375 12.9125 4.025 13.0781 4.3125 13.2469C4.6 13.4156 4.95 13.5656 5.3625 13.6969C5.775 13.8281 6.23437 13.9406 6.74062 14.0344C7.24687 14.1281 7.76875 14.1937 8.30625 14.2312Z"
    fill="currentColor"
  />
</svg>`,
		distributeSpaceHorizontal: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M15.75 15.75V2.25M2.25 15.75V2.25M6.75 6V12C6.75 12.6989 6.75 13.0483 6.86418 13.324C7.01642 13.6915 7.30844 13.9836 7.67595 14.1358C7.95165 14.25 8.30107 14.25 9 14.25C9.69893 14.25 10.0483 14.25 10.324 14.1358C10.6915 13.9836 10.9836 13.6915 11.1359 13.324C11.25 13.0483 11.25 12.6989 11.25 12V6C11.25 5.30109 11.25 4.95164 11.1359 4.67597C10.9836 4.30844 10.6915 4.01642 10.324 3.86418C10.0483 3.75 9.69893 3.75 9 3.75C8.30107 3.75 7.95165 3.75 7.67595 3.86418C7.30844 4.01642 7.01642 4.30844 6.86418 4.67597C6.75 4.95164 6.75 5.30109 6.75 6Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		distributeSpaceVertical: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M15.75 2.25H2.25M15.75 15.75H2.25M3.75 9C3.75 8.30107 3.75 7.95165 3.86418 7.67595C4.01642 7.30844 4.30844 7.01642 4.67597 6.86418C4.95164 6.75 5.30109 6.75 6 6.75H12C12.6989 6.75 13.0483 6.75 13.324 6.86418C13.6915 7.01642 13.9836 7.30844 14.1358 7.67595C14.25 7.95165 14.25 8.30107 14.25 9C14.25 9.69893 14.25 10.0483 14.1358 10.324C13.9836 10.6915 13.6915 10.9836 13.324 11.1359C13.0483 11.25 12.6989 11.25 12 11.25H6C5.30109 11.25 4.95164 11.25 4.67597 11.1359C4.30844 10.9836 4.01642 10.6915 3.86418 10.324C3.75 10.0483 3.75 9.69893 3.75 9Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		dotpoints: Q`
  <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M15.75 3.75H7.5M15.75 14.25H7.5M15.75 9H7.5M4.5 3.75C4.5 4.37132 3.99632 4.875 3.375 4.875C2.75368 4.875 2.25 4.37132 2.25 3.75C2.25 3.12868 2.75368 2.625 3.375 2.625C3.99632 2.625 4.5 3.12868 4.5 3.75ZM4.5 14.25C4.5 14.8713 3.99632 15.375 3.375 15.375C2.75368 15.375 2.25 14.8713 2.25 14.25C2.25 13.6287 2.75368 13.125 3.375 13.125C3.99632 13.125 4.5 13.6287 4.5 14.25ZM4.5 9C4.5 9.6213 3.99632 10.125 3.375 10.125C2.75368 10.125 2.25 9.6213 2.25 9C2.25 8.3787 2.75368 7.875 3.375 7.875C3.99632 7.875 4.5 8.3787 4.5 9Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
</svg>`,
		dots: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M9 9.75C9.41422 9.75 9.75 9.41422 9.75 9C9.75 8.58577 9.41422 8.25 9 8.25C8.58577 8.25 8.25 8.58577 8.25 9C8.25 9.41422 8.58577 9.75 9 9.75Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
  <path
    d="M9 4.5C9.41422 4.5 9.75 4.16421 9.75 3.75C9.75 3.33579 9.41422 3 9 3C8.58577 3 8.25 3.33579 8.25 3.75C8.25 4.16421 8.58577 4.5 9 4.5Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
  <path
    d="M9 15C9.41422 15 9.75 14.6642 9.75 14.25C9.75 13.8358 9.41422 13.5 9 13.5C8.58577 13.5 8.25 13.8358 8.25 14.25C8.25 14.6642 8.58577 15 9 15Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		download: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M2.25 15.75H15.75M9 2.25V12.75M9 12.75L14.25 7.5M9 12.75L3.75 7.5"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		dragHandle: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M2.25 6.375H15.75M2.25 11.625H15.75"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		dragPan: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M9 16.5L5.8125 13.3125L6.88125 12.2438L8.25 13.6125V9.75H4.40625L5.775 11.1L4.6875 12.1875L1.5 9L4.66875 5.83125L5.7375 6.9L4.3875 8.25H8.25V4.3875L6.88125 5.75625L5.8125 4.6875L9 1.5L12.1875 4.6875L11.1188 5.75625L9.75 4.3875V8.25H13.5938L12.225 6.9L13.3125 5.8125L16.5 9L13.3125 12.1875L12.2438 11.1188L13.6125 9.75H9.75V13.5938L11.1 12.225L12.1875 13.3125L9 16.5Z"
    fill="currentColor"
  />
</svg>`,
		expandAlt: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M12 6L15.75 2.25M15.75 2.25H12M15.75 2.25V6M6 6L2.25 2.25M2.25 2.25V6M2.25 2.25H6M6 12L2.25 15.75M2.25 15.75H6M2.25 15.75V12M12 12L15.75 15.75M15.75 15.75V12M15.75 15.75H12"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		eyeOff: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M8.05717 3.81924C8.36205 3.77417 8.67645 3.75 9.0003 3.75C12.829 3.75 15.3415 7.12863 16.1855 8.4651C16.2877 8.62688 16.3387 8.70773 16.3673 8.83253C16.3888 8.9262 16.3888 9.07403 16.3673 9.1677C16.3387 9.29243 16.2873 9.37388 16.1844 9.5367C15.9595 9.89258 15.6166 10.3928 15.1624 10.9354M5.04324 5.03628C3.42169 6.13628 2.32084 7.66455 1.81583 8.46398C1.71321 8.62643 1.6619 8.70765 1.6333 8.83237C1.61182 8.92605 1.61182 9.0738 1.63328 9.16755C1.66187 9.29228 1.71295 9.37312 1.8151 9.5349C2.65915 10.8714 5.17156 14.25 9.0003 14.25C10.5441 14.25 11.8739 13.7007 12.9666 12.9574M2.25031 2.25L15.7503 15.75M7.40932 7.40901C7.00215 7.8162 6.75031 8.3787 6.75031 9C6.75031 10.2427 7.7577 11.25 9.0003 11.25C9.6216 11.25 10.1841 10.9981 10.5913 10.591"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		eye: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M1.81509 9.5349C1.71296 9.37312 1.66188 9.29228 1.63329 9.16755C1.61182 9.07388 1.61182 8.92612 1.63329 8.83245C1.66188 8.70772 1.71296 8.62688 1.81509 8.4651C2.65915 7.12863 5.17155 3.75 9.0003 3.75C12.8291 3.75 15.3415 7.12863 16.1855 8.4651C16.2877 8.62688 16.3388 8.70772 16.3673 8.83245C16.3888 8.92612 16.3888 9.07388 16.3673 9.16755C16.3388 9.29228 16.2877 9.37312 16.1855 9.5349C15.3415 10.8714 12.8291 14.25 9.0003 14.25C5.17155 14.25 2.65915 10.8714 1.81509 9.5349Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
  <path
    d="M9.00024 11.25C10.2429 11.25 11.2502 10.2427 11.2502 9C11.2502 7.75732 10.2429 6.75 9.00024 6.75C7.75757 6.75 6.75024 7.75732 6.75024 9C6.75024 10.2427 7.75757 11.25 9.00024 11.25Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		faceSmile: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <g clip-path="url(#clip0_881_4225)">
    <path
      d="M6 10.5C6 10.5 7.125 12 9 12C10.875 12 12 10.5 12 10.5M11.25 6.75H11.2575M6.75 6.75H6.7575M16.5 9C16.5 13.1421 13.1421 16.5 9 16.5C4.85786 16.5 1.5 13.1421 1.5 9C1.5 4.85786 4.85786 1.5 9 1.5C13.1421 1.5 16.5 4.85786 16.5 9ZM11.625 6.75C11.625 6.95711 11.4571 7.125 11.25 7.125C11.0429 7.125 10.875 6.95711 10.875 6.75C10.875 6.54289 11.0429 6.375 11.25 6.375C11.4571 6.375 11.625 6.54289 11.625 6.75ZM7.125 6.75C7.125 6.95711 6.95711 7.125 6.75 7.125C6.54289 7.125 6.375 6.95711 6.375 6.75C6.375 6.54289 6.54289 6.375 6.75 6.375C6.95711 6.375 7.125 6.54289 7.125 6.75Z"
      stroke="currentColor"
      stroke-width="1.5"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
  </g>
  <defs>
    <clipPath id="clip0_881_4225">
      <rect width="18" height="18" fill="white" />
    </clipPath>
  </defs>
</svg>`,
		file: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M10.5 1.70209V4.8C10.5 5.22004 10.5 5.43006 10.5818 5.59049C10.6537 5.73161 10.7683 5.84635 10.9095 5.91826C11.0699 6 11.2799 6 11.7 6H14.7979M15 7.49117V12.9C15 14.1602 15 14.7901 14.7548 15.2715C14.5391 15.6949 14.1949 16.0391 13.7715 16.2548C13.2901 16.5 12.6601 16.5 11.4 16.5H6.6C5.33988 16.5 4.70982 16.5 4.22852 16.2548C3.80516 16.0391 3.46095 15.6949 3.24524 15.2715C3 14.7901 3 14.1602 3 12.9V5.1C3 3.83988 3 3.20982 3.24524 2.72852C3.46095 2.30516 3.80516 1.96095 4.22852 1.74524C4.70982 1.5 5.33988 1.5 6.6 1.5H9.00885C9.55912 1.5 9.8343 1.5 10.0933 1.56217C10.3228 1.61729 10.5423 1.70819 10.7437 1.83156C10.9707 1.9707 11.1653 2.16527 11.5544 2.55442L13.9456 4.94558C14.3348 5.33473 14.5293 5.5293 14.6684 5.75636C14.7918 5.95767 14.8827 6.17715 14.9378 6.40673C15 6.66568 15 6.94084 15 7.49117Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		filterFunnel: Q`
  <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M1.5 3.45C1.5 3.02996 1.5 2.81994 1.58174 2.65951C1.65365 2.51839 1.76839 2.40365 1.90951 2.33174C2.06994 2.25 2.27996 2.25 2.7 2.25H15.3C15.7201 2.25 15.9301 2.25 16.0905 2.33174C16.2317 2.40365 16.3463 2.51839 16.4183 2.65951C16.5 2.81994 16.5 3.02996 16.5 3.45V3.95204C16.5 4.15364 16.5 4.25444 16.4754 4.34817C16.4535 4.43123 16.4176 4.50992 16.3691 4.58082C16.3145 4.66082 16.2383 4.72684 16.0859 4.85887L11.2891 9.01612C11.1368 9.14812 11.0606 9.2142 11.0059 9.29415C10.9574 9.3651 10.9215 9.44378 10.8996 9.5268C10.875 9.62055 10.875 9.72135 10.875 9.92295V13.8438C10.875 13.9904 10.875 14.0638 10.8514 14.1272C10.8305 14.1832 10.7965 14.2334 10.7522 14.2736C10.7021 14.3191 10.634 14.3464 10.4978 14.4009L7.94783 15.4209C7.6722 15.5311 7.53435 15.5863 7.4237 15.5632C7.32695 15.5431 7.24204 15.4857 7.18744 15.4033C7.125 15.3092 7.125 15.1606 7.125 14.8638V9.92295C7.125 9.72135 7.125 9.62055 7.10036 9.5268C7.07854 9.44378 7.04261 9.3651 6.99413 9.29415C6.93944 9.2142 6.86327 9.14812 6.71092 9.01612L1.91408 4.85887C1.76173 4.72684 1.68556 4.66082 1.63087 4.58082C1.58239 4.50992 1.54646 4.43123 1.52464 4.34817C1.5 4.25444 1.5 4.15364 1.5 3.95204V3.45Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
</svg>`,
		flexAlignBottom: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M13.125 13.2H4.875M5.85 2.25H12.15C13.4102 2.25 14.0401 2.25 14.5215 2.49524C14.9449 2.71095 15.2891 3.05516 15.5048 3.47852C15.75 3.95982 15.75 4.58988 15.75 5.85V12.15C15.75 13.4102 15.75 14.0401 15.5048 14.5215C15.2891 14.9449 14.9449 15.2891 14.5215 15.5048C14.0401 15.75 13.4102 15.75 12.15 15.75H5.85C4.58988 15.75 3.95982 15.75 3.47852 15.5048C3.05516 15.2891 2.71095 14.9449 2.49524 14.5215C2.25 14.0401 2.25 13.4102 2.25 12.15V5.85C2.25 4.58988 2.25 3.95982 2.49524 3.47852C2.71095 3.05516 3.05516 2.71095 3.47852 2.49524C3.95982 2.25 4.58988 2.25 5.85 2.25Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		flexAlignLeft: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M4.875 13.125V4.875M5.85 2.25H12.15C13.4102 2.25 14.0401 2.25 14.5215 2.49524C14.9449 2.71095 15.2891 3.05516 15.5048 3.47852C15.75 3.95982 15.75 4.58988 15.75 5.85V12.15C15.75 13.4102 15.75 14.0401 15.5048 14.5215C15.2891 14.9449 14.9449 15.2891 14.5215 15.5048C14.0401 15.75 13.4102 15.75 12.15 15.75H5.85C4.58988 15.75 3.95982 15.75 3.47852 15.5048C3.05516 15.2891 2.71095 14.9449 2.49524 14.5215C2.25 14.0401 2.25 13.4102 2.25 12.15V5.85C2.25 4.58988 2.25 3.95982 2.49524 3.47852C2.71095 3.05516 3.05516 2.71095 3.47852 2.49524C3.95982 2.25 4.58988 2.25 5.85 2.25Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		flexAlignRight: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M13.125 13.125V4.875M5.85 2.25H12.15C13.4102 2.25 14.0401 2.25 14.5215 2.49524C14.9449 2.71095 15.2891 3.05516 15.5048 3.47852C15.75 3.95982 15.75 4.58988 15.75 5.85V12.15C15.75 13.4102 15.75 14.0401 15.5048 14.5215C15.2891 14.9449 14.9449 15.2891 14.5215 15.5048C14.0401 15.75 13.4102 15.75 12.15 15.75H5.85C4.58988 15.75 3.95982 15.75 3.47852 15.5048C3.05516 15.2891 2.71095 14.9449 2.49524 14.5215C2.25 14.0401 2.25 13.4102 2.25 12.15V5.85C2.25 4.58988 2.25 3.95982 2.49524 3.47852C2.71095 3.05516 3.05516 2.71095 3.47852 2.49524C3.95982 2.25 4.58988 2.25 5.85 2.25Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		gridAlt: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M2.25 9H15.75M9 2.25V15.75M5.85 2.25H12.15C13.4102 2.25 14.0401 2.25 14.5215 2.49524C14.9449 2.71095 15.2891 3.05516 15.5048 3.47852C15.75 3.95982 15.75 4.58988 15.75 5.85V12.15C15.75 13.4102 15.75 14.0401 15.5048 14.5215C15.2891 14.9449 14.9449 15.2891 14.5215 15.5048C14.0401 15.75 13.4102 15.75 12.15 15.75H5.85C4.58988 15.75 3.95982 15.75 3.47852 15.5048C3.05516 15.2891 2.71095 14.9449 2.49524 14.5215C2.25 14.0401 2.25 13.4102 2.25 12.15V5.85C2.25 4.58988 2.25 3.95982 2.49524 3.47852C2.71095 3.05516 3.05516 2.71095 3.47852 2.49524C3.95982 2.25 4.58988 2.25 5.85 2.25Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		gridDotsBlank: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M2.25 2.25H2.2575M2.25 9H2.2575M2.25 15.75H2.2575M2.25 12.375H2.2575M2.25 5.625H2.2575M5.625 2.25H5.6325M5.625 9H5.6325M5.625 15.75H5.6325M12.375 2.25H12.3825M12.375 9H12.3825M12.375 15.75H12.3825M9 2.25H9.0075M9 9H9.0075M9 15.75H9.0075M9 12.375H9.0075M9 5.625H9.0075M15.75 2.25H15.7575M15.75 9H15.7575M15.75 15.75H15.7575M15.75 12.375H15.7575M15.75 5.625H15.7575"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		gridDotsBottom: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M2.25 2.25H2.2575M2.25 9H2.2575M2.25 12.375H2.2575M2.25 5.625H2.2575M5.625 2.25H5.6325M5.625 9H5.6325M12.375 2.25H12.3825M12.375 9H12.3825M9 2.25H9.0075M9 9H9.0075M9 12.375H9.0075M9 5.625H9.0075M15.75 2.25H15.7575M15.75 9H15.7575M15.75 12.375H15.7575M15.75 5.625H15.7575M15.75 15.75H2.25"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		gridDotsHorizontal: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M2.25 2.25H2.2575M2.25 9H2.2575M2.25 15.75H2.2575M2.25 12.375H2.2575M2.25 5.625H2.2575M5.625 2.25H5.6325M5.625 9H5.6325M5.625 15.75H5.6325M12.375 2.25H12.3825M12.375 9H12.3825M12.375 15.75H12.3825M15.75 2.25H15.7575M15.75 9H15.7575M15.75 15.75H15.7575M15.75 12.375H15.7575M15.75 5.625H15.7575M9 15.75V2.25"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		gridDotsLeft: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M5.625 2.25H5.6325M5.625 9H5.6325M5.625 15.75H5.6325M12.375 2.25H12.3825M12.375 9H12.3825M12.375 15.75H12.3825M9 2.25H9.0075M9 9H9.0075M9 15.75H9.0075M9 12.375H9.0075M9 5.625H9.0075M15.75 2.25H15.7575M15.75 9H15.7575M15.75 15.75H15.7575M15.75 12.375H15.7575M15.75 5.625H15.7575M2.25 15.75V2.25"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		gridDotsRight: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M5.625 2.25H5.6325M5.625 9H5.6325M5.625 15.75H5.6325M12.375 2.25H12.3825M12.375 9H12.3825M12.375 15.75H12.3825M9 2.25H9.0075M9 9H9.0075M9 15.75H9.0075M9 12.375H9.0075M9 5.625H9.0075M2.25 2.25H2.2575M2.25 9H2.2575M2.25 15.75H2.2575M2.25 12.375H2.2575M2.25 5.625H2.2575M15.75 15.75V2.25"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		gridDotsTop: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M2.25 15.75H2.2575M2.25 9H2.2575M2.25 12.375H2.2575M2.25 5.625H2.2575M5.625 15.75H5.6325M5.625 9H5.6325M12.375 15.75H12.3825M12.375 9H12.3825M9 15.75H9.0075M9 9H9.0075M9 12.375H9.0075M9 5.625H9.0075M15.75 15.75H15.7575M15.75 9H15.7575M15.75 12.375H15.7575M15.75 5.625H15.7575M15.75 2.25H2.25"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		gridDotsVertical: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M2.25 2.25H2.2575M2.25 15.75H2.2575M2.25 12.375H2.2575M2.25 5.625H2.2575M5.625 2.25H5.6325M5.625 15.75H5.6325M12.375 2.25H12.3825M12.375 15.75H12.3825M9 2.25H9.0075M9 15.75H9.0075M9 12.375H9.0075M9 5.625H9.0075M15.75 2.25H15.7575M15.75 15.75H15.7575M15.75 12.375H15.7575M15.75 5.625H15.7575M15.75 9H2.25"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		hash: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M3 6H15M3 12H15M6 2.25V15.75M12 2.25V15.75"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		heading: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M4.5 3V15M13.5 3V15M6 3H3M13.5 9H4.5M6 15H3M15 15H12M15 3H12"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		heightFill: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M5.75 6.25L9 3M9 15L12.25 12.25M9 15L5.75 12.25M9 15V3M9 3L12.25 6.25"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		heightFixed: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M9 15.125V2.87503M9 2.87503L12.2501 2.87501M9 2.87503L5.75 2.875M12.2501 15.125H5.75"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		heightHug: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M5.25 15L9 11.25L12.75 15M5.25 3L9 6.75L12.75 3"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		hexagon: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M8.41725 1.82383C8.62995 1.70566 8.7363 1.64657 8.84888 1.62341C8.94863 1.60291 9.05137 1.60291 9.15112 1.62341C9.2637 1.64657 9.37005 1.70566 9.58275 1.82383L15.1328 4.90716C15.3575 5.03196 15.4697 5.09436 15.5515 5.18312C15.6238 5.26163 15.6787 5.35469 15.7121 5.45608C15.75 5.57067 15.75 5.69917 15.75 5.95615V12.0439C15.75 12.301 15.75 12.4295 15.7121 12.5441C15.6787 12.6455 15.6238 12.7385 15.5515 12.817C15.4697 12.9058 15.3575 12.9682 15.1328 13.093L9.58275 16.1763C9.37005 16.2945 9.2637 16.3535 9.15112 16.3767C9.05137 16.3972 8.94863 16.3972 8.84888 16.3767C8.7363 16.3535 8.62995 16.2945 8.41725 16.1763L2.86723 13.093C2.64259 12.9682 2.53027 12.9058 2.44847 12.817C2.37612 12.7385 2.32136 12.6455 2.28786 12.5441C2.25 12.4295 2.25 12.301 2.25 12.0439V5.95615C2.25 5.69917 2.25 5.57067 2.28786 5.45608C2.32136 5.35469 2.37612 5.26163 2.44847 5.18312C2.53027 5.09436 2.64259 5.03196 2.86723 4.90716L8.41725 1.82383Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		horizontalBarChart: Q`
  <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M12.75 7.125V4.575C12.75 4.15496 12.75 3.94494 12.6683 3.78451C12.5963 3.64339 12.4817 3.52865 12.3405 3.45674C12.1801 3.375 11.9701 3.375 11.55 3.375H2.25M9.75 10.875V13.425C9.75 13.8451 9.75 14.0551 9.66825 14.2155C9.59632 14.3567 9.48165 14.4713 9.3405 14.5433C9.18008 14.625 8.97007 14.625 8.55 14.625H2.25M2.25 1.5V16.5M2.25 10.875H14.55C14.9701 10.875 15.1801 10.875 15.3405 10.7932C15.4817 10.7213 15.5963 10.6067 15.6683 10.4655C15.75 10.3051 15.75 10.0951 15.75 9.675V8.325C15.75 7.90493 15.75 7.69492 15.6683 7.5345C15.5963 7.39339 15.4817 7.27865 15.3405 7.20674C15.1801 7.125 14.9701 7.125 14.55 7.125H2.25V10.875Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
</svg>`,
		horizontalBottom: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M4.5 15V7.5"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
  <path
    d="M9 15V3"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
  <path
    d="M13.5 15V10.5"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		horizontalCenter: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M4.5 12.75V5.25"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
  <path
    d="M9 15V3"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
  <path
    d="M13.5 11.25V6.75"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		horizontalTop: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M4.5 10.5V3"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
  <path
    d="M9 15V3"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
  <path
    d="M13.5 7.5V3"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		html: Q`
<svg xmlns="http://www.w3.org/2000/svg" height="18" viewBox="0 -960 960 960" width="18" fill="currentColor">
  <path d="M0-360v-240h60v80h80v-80h60v240h-60v-100H60v100H0Zm310 0v-180h-70v-60h200v60h-70v180h-60Zm170 0v-200q0-17 11.5-28.5T520-600h180q17 0 28.5 11.5T740-560v200h-60v-180h-40v140h-60v-140h-40v180h-60Zm320 0v-240h60v180h100v60H800Z"/>
</svg>`,
		imageIndentLeft: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M15.75 6.9375H11.25M15.75 3H2.25M15.75 11.0625H11.25M15.75 15H2.25M3.45 12H7.05C7.47004 12 7.68008 12 7.8405 11.9182C7.98165 11.8463 8.09632 11.7317 8.16825 11.5905C8.25 11.4301 8.25 11.2201 8.25 10.8V7.2C8.25 6.77996 8.25 6.56994 8.16825 6.40951C8.09632 6.26839 7.98165 6.15365 7.8405 6.08174C7.68008 6 7.47004 6 7.05 6H3.45C3.02996 6 2.81994 6 2.65951 6.08174C2.51839 6.15365 2.40365 6.26839 2.33174 6.40951C2.25 6.56994 2.25 6.77996 2.25 7.2V10.8C2.25 11.2201 2.25 11.4301 2.33174 11.5905C2.40365 11.7317 2.51839 11.8463 2.65951 11.9182C2.81994 12 3.02996 12 3.45 12Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		layoutTop: Q`
  <svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M2.25 6.75H15.75M5.85 2.25H12.15C13.4102 2.25 14.0401 2.25 14.5215 2.49524C14.9449 2.71095 15.2891 3.05516 15.5048 3.47852C15.75 3.95982 15.75 4.58988 15.75 5.85V12.15C15.75 13.4102 15.75 14.0401 15.5048 14.5215C15.2891 14.9449 14.9449 15.2891 14.5215 15.5048C14.0401 15.75 13.4102 15.75 12.15 15.75H5.85C4.58988 15.75 3.95982 15.75 3.47852 15.5048C3.05516 15.2891 2.71095 14.9449 2.49524 14.5215C2.25 14.0401 2.25 13.4102 2.25 12.15V5.85C2.25 4.58988 2.25 3.95982 2.49524 3.47852C2.71095 3.05516 3.05516 2.71095 3.47852 2.49524C3.95982 2.25 4.58988 2.25 5.85 2.25Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		layout: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M2.25 6.75H15.75M6.75 6.75V15.75M5.85 2.25H12.15C13.4102 2.25 14.0401 2.25 14.5215 2.49524C14.9449 2.71095 15.2891 3.05516 15.5048 3.47852C15.75 3.95982 15.75 4.58988 15.75 5.85V12.15C15.75 13.4102 15.75 14.0401 15.5048 14.5215C15.2891 14.9449 14.9449 15.2891 14.5215 15.5048C14.0401 15.75 13.4102 15.75 12.15 15.75H5.85C4.58988 15.75 3.95982 15.75 3.47852 15.5048C3.05516 15.2891 2.71095 14.9449 2.49524 14.5215C2.25 14.0401 2.25 13.4102 2.25 12.15V5.85C2.25 4.58988 2.25 3.95982 2.49524 3.47852C2.71095 3.05516 3.05516 2.71095 3.47852 2.49524C3.95982 2.25 4.58988 2.25 5.85 2.25Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		letterSpacing: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M6.75 9.75H11.25M5.25 12.75L8.45377 5.70169C8.62732 5.31992 8.7141 5.12904 8.83433 5.06982C8.9388 5.01836 9.0612 5.01836 9.16567 5.06982C9.2859 5.12904 9.37268 5.31992 9.54623 5.70169L12.75 12.75M15.75 2.25V15.75M2.25 2.25V15.75"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		lightning: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M9.75003 1.5L3.07011 9.51592C2.8085 9.8298 2.6777 9.98678 2.6757 10.1194C2.67396 10.2346 2.72532 10.3442 2.81496 10.4167C2.91808 10.5 3.1224 10.5 3.53105 10.5H9.00003L8.25003 16.5L14.9299 8.48408C15.1915 8.1702 15.3223 8.01322 15.3243 7.88062C15.3261 7.76542 15.2748 7.65578 15.1851 7.58333C15.0819 7.5 14.8776 7.5 14.469 7.5H9.00003L9.75003 1.5Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		lineChart: Q`
  <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M15.75 15.75H3.45C3.02996 15.75 2.81994 15.75 2.65951 15.6683C2.51839 15.5963 2.40365 15.4817 2.33174 15.3405C2.25 15.1801 2.25 14.9701 2.25 14.55V2.25M15 6L12.0608 9.13703C11.9495 9.2559 11.8938 9.3153 11.8266 9.34605C11.7673 9.3732 11.7019 9.38437 11.637 9.37852C11.5634 9.37185 11.4911 9.33435 11.3465 9.25928L8.90347 7.99072C8.75887 7.91565 8.68658 7.87815 8.613 7.87148C8.54805 7.86563 8.48273 7.8768 8.4234 7.90395C8.3562 7.9347 8.30055 7.9941 8.18917 8.11297L5.25 11.25" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
</svg>`,
		lineHeight: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M6.75 9.75H11.25M5.25 12.75L8.45377 5.70168C8.62732 5.31992 8.7141 5.12904 8.83433 5.06982C8.9388 5.01836 9.0612 5.01836 9.16567 5.06982C9.2859 5.12904 9.37268 5.31992 9.54623 5.70168L12.75 12.75M15.75 15.75H2.25M15.75 2.25H2.25"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		lowPriority: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M3 8.60625C3 9.49375 3.29688 10.2563 3.89062 10.8938C4.48438 11.5313 5.225 11.8875 6.1125 11.9625L4.95 10.8L6 9.75L9 12.75L6 15.75L4.95 14.7L6.15 13.5C4.8375 13.425 3.73438 12.9188 2.84063 11.9813C1.94688 11.0438 1.5 9.925 1.5 8.625C1.5 7.2625 1.97188 6.10938 2.91563 5.16563C3.85938 4.22188 5.0125 3.75 6.375 3.75H9V5.25H6.375C5.4375 5.25 4.64063 5.575 3.98438 6.225C3.32813 6.875 3 7.66875 3 8.60625ZM10.5 13.5V12H16.5V13.5H10.5ZM10.5 9.375V7.875H16.5V9.375H10.5ZM10.5 5.25V3.75H16.5V5.25H10.5Z"
    fill="currentColor"
  />
</svg>`,
		magic: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M9.75002 10.5L7.50002 8.25M11.2577 2.625V1.5M14.2123 3.79549L15.0077 3M14.2123 9.75L15.0077 10.5455M8.25774 3.79549L7.46228 3M15.3827 6.75H16.5077M4.59854 15.6515L11.5265 8.72355C11.8235 8.42655 11.972 8.27798 12.0276 8.10675C12.0766 7.95615 12.0766 7.79385 12.0276 7.64325C11.972 7.47199 11.8235 7.32349 11.5265 7.02647L10.9736 6.47353C10.6765 6.17651 10.528 6.02801 10.3568 5.97237C10.2062 5.92343 10.0439 5.92343 9.89327 5.97237C9.72197 6.02801 9.57347 6.17651 9.27647 6.47353L2.34854 13.4015C2.05153 13.6985 1.90302 13.847 1.84738 14.0182C1.79843 14.1688 1.79843 14.3312 1.84738 14.4818C1.90302 14.653 2.05153 14.8015 2.34854 15.0985L2.90148 15.6515C3.1985 15.9485 3.347 16.097 3.51825 16.1526C3.66888 16.2016 3.83114 16.2016 3.98177 16.1526C4.15302 16.097 4.30153 15.9485 4.59854 15.6515Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		map: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <g clip-path="url(#clip0_881_4213)">
    <path
      d="M6.75 13.5L1.5 16.5V4.5L6.75 1.5M6.75 13.5L12 16.5M6.75 13.5V1.5M6.75 1.5L12 4.5M12 16.5L16.5 13.5V1.5L12 4.5M12 16.5V4.5"
      stroke="currentColor"
      stroke-width="1.5"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
  </g>
  <defs>
    <clipPath id="clip0_881_4213">
      <rect width="18" height="18" fill="white" />
    </clipPath>
  </defs>
</svg>`,
		menuAlt: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M2.25 9H15.75M2.25 4.5H15.75M2.25 13.5H11.25"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		mouse: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M9 16.5C7.55 16.5 6.3125 15.9875 5.2875 14.9625C4.2625 13.9375 3.75 12.7 3.75 11.25V6.75C3.75 5.3 4.2625 4.0625 5.2875 3.0375C6.3125 2.0125 7.55 1.5 9 1.5C10.45 1.5 11.6875 2.0125 12.7125 3.0375C13.7375 4.0625 14.25 5.3 14.25 6.75V11.25C14.25 12.7 13.7375 13.9375 12.7125 14.9625C11.6875 15.9875 10.45 16.5 9 16.5ZM9.75 6.75H12.75C12.75 5.85 12.4656 5.05625 11.8969 4.36875C11.3281 3.68125 10.6125 3.25 9.75 3.075V6.75ZM5.25 6.75H8.25V3.075C7.3875 3.25 6.67188 3.68125 6.10313 4.36875C5.53438 5.05625 5.25 5.85 5.25 6.75ZM9 15C10.0375 15 10.9219 14.6344 11.6531 13.9031C12.3844 13.1719 12.75 12.2875 12.75 11.25V8.25H5.25V11.25C5.25 12.2875 5.61563 13.1719 6.34688 13.9031C7.07813 14.6344 7.9625 15 9 15Z"
    fill="currentColor"
  />
</svg>`,
		paddingBottom: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M13.5 15.75H4.5"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
  <rect
    x="6.75"
    y="6.75"
    width="4.5"
    height="4.5"
    rx="1.25"
    stroke="currentColor"
    stroke-width="1.5"
  />
</svg>`,
		paddingHorizontal: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <rect
    x="6.75"
    y="6.75"
    width="4.5"
    height="4.5"
    rx="1.25"
    stroke="currentColor"
    stroke-width="1.5"
  />
  <path
    d="M15.75 13.5V4.5M2.25 13.5V4.5"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		paddingLeft: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <rect
    x="6.75"
    y="6.75"
    width="4.5"
    height="4.5"
    rx="1.25"
    stroke="currentColor"
    stroke-width="1.5"
  />
  <path
    d="M2.25 13.5V4.5"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		paddingRight: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <rect
    x="6.75"
    y="6.75"
    width="4.5"
    height="4.5"
    rx="1.25"
    stroke="currentColor"
    stroke-width="1.5"
  />
  <path
    d="M15.75 13.5V4.5"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		paddingTop: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M13.5 2.25H4.5"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
  <rect
    x="6.75"
    y="6.75"
    width="4.5"
    height="4.5"
    rx="1.25"
    stroke="currentColor"
    stroke-width="1.5"
  />
</svg>`,
		paddingVertical: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M13.5 2.25H4.5M13.5 15.75H4.5"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
  <rect
    x="6.75"
    y="6.75"
    width="4.5"
    height="4.5"
    rx="1.25"
    stroke="currentColor"
    stroke-width="1.5"
  />
</svg>`,
		padding: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M13.5 2.25H4.5M13.5 15.75H4.5"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
  <rect
    x="6.75"
    y="6.75"
    width="4.5"
    height="4.5"
    rx="1.25"
    stroke="currentColor"
    stroke-width="1.5"
  />
  <path
    d="M15.75 13.5V4.5M2.25 13.5V4.5"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		passcode: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M9 9H9.00375M12.75 9H12.7537M5.25 9H5.25375M3.9 5.25H14.1C14.9401 5.25 15.3602 5.25 15.681 5.41349C15.9632 5.5573 16.1927 5.78677 16.3365 6.06902C16.5 6.38988 16.5 6.80992 16.5 7.65V10.35C16.5 11.1901 16.5 11.6101 16.3365 11.931C16.1927 12.2132 15.9632 12.4427 15.681 12.5865C15.3602 12.75 14.9401 12.75 14.1 12.75H3.9C3.05992 12.75 2.63988 12.75 2.31902 12.5865C2.03677 12.4427 1.8073 12.2132 1.66349 11.931C1.5 11.6101 1.5 11.1901 1.5 10.35V7.65C1.5 6.80992 1.5 6.38988 1.66349 6.06902C1.8073 5.78677 2.03677 5.5573 2.31902 5.41349C2.63988 5.25 3.05992 5.25 3.9 5.25ZM9.1875 9C9.1875 9.10357 9.10357 9.1875 9 9.1875C8.89643 9.1875 8.8125 9.10357 8.8125 9C8.8125 8.89643 8.89643 8.8125 9 8.8125C9.10357 8.8125 9.1875 8.89643 9.1875 9ZM12.9375 9C12.9375 9.10357 12.8536 9.1875 12.75 9.1875C12.6464 9.1875 12.5625 9.10357 12.5625 9C12.5625 8.89643 12.6464 8.8125 12.75 8.8125C12.8536 8.8125 12.9375 8.89643 12.9375 9ZM5.4375 9C5.4375 9.10357 5.35355 9.1875 5.25 9.1875C5.14645 9.1875 5.0625 9.10357 5.0625 9C5.0625 8.89643 5.14645 8.8125 5.25 8.8125C5.35355 8.8125 5.4375 8.89643 5.4375 9Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		phone: Q`
  <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M6.28521 6.64001C6.8072 7.72725 7.51882 8.7462 8.41995 9.6474C9.32115 10.5486 10.3402 11.2602 11.4274 11.7822C11.5209 11.8271 11.5676 11.8496 11.6268 11.8668C11.8371 11.9281 12.0952 11.884 12.2733 11.7565C12.3235 11.7206 12.3663 11.6778 12.452 11.5921C12.7142 11.3298 12.8453 11.1987 12.9772 11.113C13.4743 10.7898 14.1153 10.7898 14.6125 11.113C14.7443 11.1987 14.8754 11.3298 15.1376 11.5921L15.2837 11.7382C15.6823 12.1368 15.8816 12.3361 15.9898 12.5501C16.2052 12.9757 16.2052 13.4785 15.9898 13.9042C15.8816 14.1182 15.6823 14.3175 15.2837 14.7161L15.1655 14.8342C14.7683 15.2315 14.5697 15.4301 14.2996 15.5818C14 15.7501 13.5346 15.8711 13.191 15.8701C12.8813 15.8692 12.6696 15.8091 12.2463 15.689C9.97125 15.0432 7.82445 13.8249 6.03349 12.0339C4.24251 10.2429 3.02415 8.0961 2.37843 5.82108C2.25827 5.39774 2.19819 5.18608 2.19727 4.87637C2.19624 4.5327 2.31726 4.06733 2.48558 3.7677C2.63727 3.49768 2.83587 3.29907 3.2331 2.90185L3.35132 2.78362C3.7499 2.38504 3.9492 2.18575 4.16323 2.07749C4.5889 1.86219 5.0916 1.86219 5.51727 2.07749C5.7313 2.18575 5.9306 2.38504 6.32918 2.78362L6.47533 2.92978C6.73753 3.19198 6.86864 3.32309 6.95435 3.45491C7.2776 3.95209 7.2776 4.59302 6.95435 5.09019C6.86864 5.22202 6.73753 5.35313 6.47533 5.61533C6.3896 5.70107 6.34673 5.74393 6.31085 5.79403C6.18334 5.97211 6.13932 6.2303 6.20061 6.44057C6.21786 6.49974 6.2403 6.54649 6.28521 6.64001Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
</svg>
  `,
		pinAlt: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M6.28255 11.7123L2.03992 15.9549M8.77083 4.98139L7.60015 6.15205C7.50468 6.24755 7.45692 6.2953 7.40252 6.33323C7.35424 6.36691 7.30215 6.39478 7.24735 6.41628C7.18561 6.4405 7.1194 6.45374 6.98697 6.48022L4.23862 7.02989C3.52439 7.17274 3.16728 7.24416 3.00021 7.43245C2.85466 7.59648 2.78819 7.816 2.81831 8.0332C2.85288 8.28258 3.11039 8.54005 3.62543 9.05515L8.93973 14.3694C9.45475 14.8845 9.7123 15.142 9.9616 15.1766C10.1789 15.2067 10.3984 15.1402 10.5624 14.9946C10.7507 14.8276 10.8221 14.4705 10.965 13.7562L11.5146 11.0079C11.5411 10.8755 11.5544 10.8093 11.5786 10.7475C11.6001 10.6927 11.628 10.6407 11.6616 10.5924C11.6996 10.5379 11.7473 10.4902 11.8428 10.3947L13.0134 9.22405C13.0745 9.163 13.1051 9.13248 13.1386 9.10578C13.1684 9.08215 13.2 9.06078 13.233 9.04188C13.2702 9.02058 13.3099 9.00363 13.3893 8.96958L15.2601 8.16783C15.8058 7.9339 16.0788 7.81698 16.2027 7.62798C16.3111 7.46272 16.3499 7.26134 16.3106 7.06764C16.2657 6.84613 16.0557 6.63619 15.6359 6.21632L11.7786 2.35897C11.3586 1.9391 11.1487 1.72917 10.9272 1.68427C10.7335 1.645 10.5321 1.68378 10.3669 1.79218C10.1779 1.91614 10.0609 2.18902 9.82705 2.7348L9.0253 4.60556C8.99125 4.68493 8.97423 4.72461 8.953 4.76182C8.9341 4.79488 8.91273 4.82644 8.88903 4.85625C8.8624 4.88981 8.83188 4.92034 8.77083 4.98139Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		pin: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M9.0003 11.25V16.5V11.25ZM6.00032 5.4811V7.07906C6.00032 7.23508 6.00032 7.31308 5.985 7.38769C5.97142 7.45389 5.94895 7.51793 5.91821 7.57815C5.88355 7.64595 5.83482 7.70685 5.73736 7.82872L4.56003 9.30037C4.06072 9.92453 3.81107 10.2365 3.81079 10.4992C3.81054 10.7276 3.91439 10.9437 4.0929 11.0861C4.29816 11.25 4.69779 11.25 5.49707 11.25H12.5035C13.3028 11.25 13.7025 11.25 13.9078 11.0861C14.0863 10.9437 14.1901 10.7276 14.1898 10.4992C14.1895 10.2365 13.9399 9.92453 13.4406 9.30037L12.2632 7.82872C12.1658 7.70685 12.1171 7.64595 12.0824 7.57815C12.0517 7.51793 12.0292 7.45389 12.0156 7.38769C12.0003 7.31308 12.0003 7.23508 12.0003 7.07906V5.4811C12.0003 5.39476 12.0003 5.35158 12.0052 5.30901C12.0095 5.27119 12.0167 5.23375 12.0268 5.19703C12.038 5.15568 12.0541 5.1156 12.0862 5.03543L12.842 3.14567C13.0626 2.59436 13.1728 2.31869 13.1269 2.09741C13.0867 1.9039 12.9717 1.73408 12.8069 1.62488C12.6185 1.5 12.3217 1.5 11.7279 1.5H6.27276C5.67897 1.5 5.38208 1.5 5.1937 1.62488C5.02896 1.73408 4.91399 1.9039 4.87378 2.09741C4.8278 2.31869 4.93806 2.59436 5.15859 3.14567L5.91449 5.03543C5.94656 5.1156 5.96259 5.15568 5.97387 5.19703C5.98389 5.23375 5.9911 5.27119 5.99544 5.30901C6.00032 5.35158 6.00032 5.39476 6.00032 5.4811Z"
    fill="currentColor"
  />
  <path
    d="M9.0003 11.25V16.5M6.00032 5.4811V7.07906C6.00032 7.23508 6.00032 7.31308 5.985 7.38769C5.97142 7.45389 5.94895 7.51793 5.91821 7.57815C5.88355 7.64595 5.83482 7.70685 5.73736 7.82873L4.56003 9.30038C4.06072 9.92453 3.81107 10.2365 3.81079 10.4992C3.81054 10.7276 3.91439 10.9437 4.0929 11.0861C4.29816 11.25 4.69779 11.25 5.49707 11.25H12.5035C13.3028 11.25 13.7025 11.25 13.9078 11.0861C14.0863 10.9437 14.1901 10.7276 14.1898 10.4992C14.1895 10.2365 13.9399 9.92453 13.4406 9.30038L12.2632 7.82873C12.1658 7.70685 12.1171 7.64595 12.0824 7.57815C12.0517 7.51793 12.0292 7.45389 12.0156 7.38769C12.0003 7.31308 12.0003 7.23508 12.0003 7.07906V5.4811C12.0003 5.39476 12.0003 5.35158 12.0052 5.30901C12.0095 5.27119 12.0167 5.23375 12.0268 5.19703C12.038 5.15568 12.0541 5.1156 12.0862 5.03543L12.842 3.14567C13.0626 2.59436 13.1728 2.31869 13.1269 2.09741C13.0867 1.9039 12.9717 1.73408 12.8069 1.62488C12.6185 1.5 12.3217 1.5 11.7279 1.5H6.27276C5.67897 1.5 5.38208 1.5 5.1937 1.62488C5.02896 1.73408 4.91399 1.9039 4.87378 2.09741C4.8278 2.31869 4.93806 2.59436 5.15859 3.14567L5.91449 5.03543C5.94656 5.1156 5.96259 5.15568 5.97387 5.19703C5.98389 5.23375 5.9911 5.27119 5.99544 5.30901C6.00032 5.35158 6.00032 5.39476 6.00032 5.4811Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		play: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M3.75 3.74213C3.75 3.01376 3.75 2.64958 3.90187 2.44882C4.03417 2.27393 4.23639 2.16571 4.4553 2.15263C4.70658 2.13763 5.0096 2.33965 5.61564 2.74367L13.5023 8.00145C14.0031 8.33527 14.2534 8.50222 14.3407 8.7126C14.417 8.89657 14.417 9.10327 14.3407 9.28725C14.2534 9.49762 14.0031 9.6645 13.5023 9.9984L5.61564 15.2561C5.0096 15.6601 4.70658 15.8622 4.4553 15.8472C4.23639 15.8342 4.03417 15.7259 3.90187 15.551C3.75 15.3502 3.75 14.986 3.75 14.2577V3.74213Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		radioButtonPartial: Q`
<svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
  <path d="M9 12.75V5.25C7.9625 5.25 7.07813 5.61563 6.34688 6.34688C5.61563 7.07813 5.25 7.9625 5.25 9C5.25 10.0375 5.61563 10.9219 6.34688 11.6531C7.07813 12.3844 7.9625 12.75 9 12.75ZM9 16.5C7.9625 16.5 6.9875 16.3031 6.075 15.9094C5.1625 15.5156 4.36875 14.9813 3.69375 14.3063C3.01875 13.6313 2.48438 12.8375 2.09063 11.925C1.69688 11.0125 1.5 10.0375 1.5 9C1.5 7.9625 1.69688 6.9875 2.09063 6.075C2.48438 5.1625 3.01875 4.36875 3.69375 3.69375C4.36875 3.01875 5.1625 2.48438 6.075 2.09063C6.9875 1.69688 7.9625 1.5 9 1.5C10.0375 1.5 11.0125 1.69688 11.925 2.09063C12.8375 2.48438 13.6313 3.01875 14.3063 3.69375C14.9813 4.36875 15.5156 5.1625 15.9094 6.075C16.3031 6.9875 16.5 7.9625 16.5 9C16.5 10.0375 16.3031 11.0125 15.9094 11.925C15.5156 12.8375 14.9813 13.6313 14.3063 14.3063C13.6313 14.9813 12.8375 15.5156 11.925 15.9094C11.0125 16.3031 10.0375 16.5 9 16.5ZM9 15C10.675 15 12.0938 14.4188 13.2563 13.2563C14.4188 12.0938 15 10.675 15 9C15 7.325 14.4188 5.90625 13.2563 4.74375C12.0938 3.58125 10.675 3 9 3C7.325 3 5.90625 3.58125 4.74375 4.74375C3.58125 5.90625 3 7.325 3 9C3 10.675 3.58125 12.0938 4.74375 13.2563C5.90625 14.4188 7.325 15 9 15Z" fill="currentColor"/>
</svg>`,
		refreshAlt: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M16.5 7.5C16.5 7.5 14.9963 5.45116 13.7747 4.22868C12.553 3.0062 10.8648 2.25 9 2.25C5.27208 2.25 2.25 5.27208 2.25 9C2.25 12.728 5.27208 15.75 9 15.75C12.0773 15.75 14.6737 13.6907 15.4862 10.875M16.5 7.5V3M16.5 7.5H12"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		repeat: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M12.75 1.5L15.75 4.5M15.75 4.5L12.75 7.5M15.75 4.5H5.85C4.58988 4.5 3.95982 4.5 3.47852 4.74524C3.05516 4.96095 2.71095 5.30516 2.49524 5.72852C2.25 6.20982 2.25 6.83988 2.25 8.1V8.25M2.25 13.5H12.15C13.4102 13.5 14.0401 13.5 14.5215 13.2548C14.9449 13.0391 15.2891 12.6949 15.5048 12.2715C15.75 11.7901 15.75 11.1601 15.75 9.9V9.75M2.25 13.5L5.25 16.5M2.25 13.5L5.25 10.5"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		shoppingCart: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <g clip-path="url(#clip0_1795_16059)">
    <path
      d="M3.75011 10.5H13.6019C14.3615 10.5 14.7413 10.5 15.0437 10.3584C15.3101 10.2336 15.5338 10.0333 15.6872 9.7824C15.8614 9.49747 15.9033 9.12 15.9872 8.36505L16.426 4.41626C16.4516 4.18566 16.4644 4.07036 16.4273 3.98112C16.3948 3.90274 16.3366 3.83773 16.2623 3.79671C16.1777 3.75 16.0616 3.75 15.8296 3.75H3.37511M1.5 1.5H2.43633C2.6348 1.5 2.73403 1.5 2.81167 1.53774C2.88001 1.57097 2.93666 1.62418 2.97408 1.69032C3.01659 1.76546 3.02278 1.86449 3.03516 2.06257L3.71484 12.9374C3.72722 13.1355 3.73341 13.2346 3.77592 13.3096C3.81335 13.3758 3.86999 13.429 3.93833 13.4623C4.01597 13.5 4.11521 13.5 4.31367 13.5H14.25M5.625 16.125H5.6325M12.375 16.125H12.3825M6 16.125C6 16.3321 5.83211 16.5 5.625 16.5C5.41789 16.5 5.25 16.3321 5.25 16.125C5.25 15.9179 5.41789 15.75 5.625 15.75C5.83211 15.75 6 15.9179 6 16.125ZM12.75 16.125C12.75 16.3321 12.5821 16.5 12.375 16.5C12.1679 16.5 12 16.3321 12 16.125C12 15.9179 12.1679 15.75 12.375 15.75C12.5821 15.75 12.75 15.9179 12.75 16.125Z"
      stroke="currentColor"
      stroke-width="1.5"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
  </g>
  <defs>
    <clipPath id="clip0_1795_16059">
      <rect width="18" height="18" fill="white" />
    </clipPath>
  </defs>
</svg>`,
		slidersAlt: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M3.75 15.75V11.25M3.75 11.25C4.57843 11.25 5.25 10.5784 5.25 9.75C5.25 8.92155 4.57843 8.25 3.75 8.25C2.92157 8.25 2.25 8.92155 2.25 9.75C2.25 10.5784 2.92157 11.25 3.75 11.25ZM3.75 5.25V2.25M9 15.75V11.25M9 5.25V2.25M9 5.25C8.17155 5.25 7.5 5.92157 7.5 6.75C7.5 7.57845 8.17155 8.25 9 8.25C9.82845 8.25 10.5 7.57845 10.5 6.75C10.5 5.92157 9.82845 5.25 9 5.25ZM14.25 15.75V12.75M14.25 12.75C15.0784 12.75 15.75 12.0784 15.75 11.25C15.75 10.4216 15.0784 9.75 14.25 9.75C13.4216 9.75 12.75 10.4216 12.75 11.25C12.75 12.0784 13.4216 12.75 14.25 12.75ZM14.25 6.75V2.25"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		sliders: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M3.75 15.75V10.5M3.75 7.5V2.25M9 15.75V9M9 6V2.25M14.25 15.75V12M14.25 9V2.25M1.5 10.5H6M6.75 6H11.25M12 12H16.5"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		spacingHeight: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M9 13.5V4.5M9 13.5L6.75 12M9 13.5L11.25 12M9 4.5L6.75 6M9 4.5L11.25 6M15.75 2.25H2.25M15.75 15.75H2.25"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		spacingWidth: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M4.5 9H13.5M4.5 9L6 6.75M4.5 9L6 11.25M13.5 9L12 6.75M13.5 9L12 11.25M15.75 15.75V2.25M2.25 15.75V2.25"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		switchOff: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M1.5 9C1.5 6.51472 3.51472 4.5 6 4.5H12C14.4853 4.5 16.5 6.51472 16.5 9C16.5 11.4853 14.4853 13.5 12 13.5H6C3.51472 13.5 1.5 11.4853 1.5 9Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
  <path
    d="M6 11.5C7.38071 11.5 8.5 10.3807 8.5 9C8.5 7.6193 7.38071 6.5 6 6.5C4.61929 6.5 3.5 7.6193 3.5 9C3.5 10.3807 4.61929 11.5 6 11.5Z"
    fill="currentColor"
  />
</svg>`,
		switchOn: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M1.5 9C1.5 6.51472 3.51472 4.5 6 4.5H12C14.4853 4.5 16.5 6.51472 16.5 9C16.5 11.4853 14.4853 13.5 12 13.5H6C3.51472 13.5 1.5 11.4853 1.5 9Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
  <path
    d="M12 11.5C13.3807 11.5 14.5 10.3807 14.5 9C14.5 7.6193 13.3807 6.5 12 6.5C10.6193 6.5 9.5 7.6193 9.5 9C9.5 10.3807 10.6193 11.5 12 11.5Z"
    fill="currentColor"
  />
</svg>`,
		switchVertical: Q`
  <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M12.75 3V15M12.75 15L9.75 12M12.75 15L15.75 12M5.25 15V3M5.25 3L2.25 6M5.25 3L8.25 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
</svg>`,
		tableAlt: Q`<svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M3.75 15.75C3.3375 15.75 2.98438 15.6031 2.69063 15.3094C2.39687 15.0156 2.25 14.6625 2.25 14.25V3.75C2.25 3.3375 2.39687 2.98438 2.69063 2.69063C2.98438 2.39687 3.3375 2.25 3.75 2.25H14.25C14.6625 2.25 15.0156 2.39687 15.3094 2.69063C15.6031 2.98438 15.75 3.3375 15.75 3.75V14.25C15.75 14.6625 15.6031 15.0156 15.3094 15.3094C15.0156 15.6031 14.6625 15.75 14.25 15.75H3.75ZM8.25 11.25H3.75V14.25H8.25V11.25ZM9.75 11.25V14.25H14.25V11.25H9.75ZM8.25 9.75V6.75H3.75V9.75H8.25ZM9.75 9.75H14.25V6.75H9.75V9.75ZM3.75 5.25H14.25V3.75H3.75V5.25Z" fill="currentColor"/>
</svg>`,
		tableAltEdit: Q`<svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M3.75 9.75H8.25V6.75H3.75V9.75ZM3.75 5.25H14.25V3.75H3.75V5.25ZM3.75 15.75C3.3375 15.75 2.98438 15.6031 2.69063 15.3094C2.39687 15.0156 2.25 14.6625 2.25 14.25V3.75C2.25 3.3375 2.39687 2.98438 2.69063 2.69063C2.98438 2.39687 3.3375 2.25 3.75 2.25H14.25C14.6625 2.25 15.0156 2.39687 15.3094 2.69063C15.6031 2.98438 15.75 3.3375 15.75 3.75V8.475C15.5125 8.375 15.2656 8.30938 15.0094 8.27813C14.7531 8.24688 14.5 8.25 14.25 8.2875C13.9875 8.3375 13.7344 8.42188 13.4906 8.54063C13.2469 8.65938 13.025 8.81875 12.825 9.01875L12.0938 9.75L8.25 13.575V15.75H3.75ZM3.75 14.25H8.25V11.25H3.75V14.25ZM9.75 9.75H12.0938L12.825 9.01875C13.025 8.81875 13.2469 8.65938 13.4906 8.54063C13.7344 8.42188 13.9875 8.3375 14.25 8.2875V6.75H9.75V9.75ZM9.75 16.5V14.1938L13.8938 10.0688C14.0063 9.95625 14.1313 9.875 14.2688 9.825C14.4063 9.775 14.5438 9.75 14.6813 9.75C14.8313 9.75 14.975 9.77813 15.1125 9.83438C15.25 9.89063 15.375 9.975 15.4875 10.0875L16.1813 10.7813C16.2813 10.8938 16.3594 11.0188 16.4156 11.1563C16.4719 11.2938 16.5 11.4313 16.5 11.5688C16.5 11.7063 16.475 11.8469 16.425 11.9906C16.375 12.1344 16.2938 12.2625 16.1813 12.375L12.0563 16.5H9.75ZM10.875 15.375H11.5875L13.8563 13.0875L13.1625 12.3938L10.875 14.6625V15.375ZM13.5188 12.7313L13.1625 12.3938L13.8563 13.0875L13.5188 12.7313Z" fill="currentColor"/>
</svg>`,
		tag: Q`
  <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
<g clip-path="url(#clip0_2290_25895)">
<path d="M6 6H6.0075M1.5 3.9V7.25588C1.5 7.62278 1.5 7.80623 1.54144 7.97888C1.57819 8.13188 1.63879 8.2782 1.72104 8.41245C1.8138 8.5638 1.94351 8.69355 2.20294 8.95297L7.95442 14.7044C8.84542 15.5954 9.291 16.041 9.80467 16.2079C10.2566 16.3547 10.7434 16.3547 11.1953 16.2079C11.709 16.041 12.1546 15.5954 13.0456 14.7044L14.7044 13.0456C15.5954 12.1546 16.041 11.709 16.2079 11.1953C16.3547 10.7434 16.3547 10.2566 16.2079 9.80467C16.041 9.291 15.5954 8.84542 14.7044 7.95442L8.95297 2.20294C8.69355 1.94351 8.5638 1.8138 8.41245 1.72104C8.2782 1.63879 8.13188 1.57819 7.97888 1.54144C7.80623 1.5 7.62277 1.5 7.25589 1.5H3.9C3.05992 1.5 2.63988 1.5 2.31902 1.66349C2.03678 1.8073 1.8073 2.03677 1.66349 2.31902C1.5 2.63988 1.5 3.05992 1.5 3.9ZM6.375 6C6.375 6.20711 6.20711 6.375 6 6.375C5.79289 6.375 5.625 6.20711 5.625 6C5.625 5.79289 5.79289 5.625 6 5.625C6.20711 5.625 6.375 5.79289 6.375 6Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
</g>
<defs>
<clipPath id="clip0_2290_25895">
<rect width="18" height="18" fill="white"/>
</clipPath>
</defs>
</svg>

  `,
		textInput: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M9.75 5.25H3.9C3.05992 5.25 2.63988 5.25 2.31902 5.41349C2.03677 5.5573 1.8073 5.78677 1.66349 6.06902C1.5 6.38988 1.5 6.80992 1.5 7.65V10.35C1.5 11.1901 1.5 11.6101 1.66349 11.931C1.8073 12.2132 2.03677 12.4427 2.31902 12.5865C2.63988 12.75 3.05992 12.75 3.9 12.75H9.75M12.75 5.25H14.1C14.9401 5.25 15.3602 5.25 15.681 5.41349C15.9632 5.5573 16.1927 5.78677 16.3365 6.06902C16.5 6.38988 16.5 6.80992 16.5 7.65V10.35C16.5 11.1901 16.5 11.6101 16.3365 11.931C16.1927 12.2132 15.9632 12.4427 15.681 12.5865C15.3602 12.75 14.9401 12.75 14.1 12.75H12.75M12.75 15.75V2.25M14.625 2.25001L10.875 2.25M14.625 15.75H10.875"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		thumbsDownAlt: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M12.7501 1.5V9.75V1.5ZM16.5001 7.35V3.9C16.5001 3.05992 16.5001 2.63988 16.3366 2.31901C16.1927 2.03677 15.9633 1.8073 15.681 1.66349C15.3602 1.5 14.9402 1.5 14.1001 1.5H6.08855C4.99244 1.5 4.44438 1.5 4.00172 1.70057C3.61159 1.87736 3.28001 2.16181 3.04597 2.52054C2.78042 2.92754 2.69708 3.46923 2.53041 4.55259L2.1381 7.10259C1.91828 8.53147 1.80836 9.24592 2.0204 9.80182C2.20649 10.2898 2.55653 10.6978 3.01049 10.9559C3.52769 11.25 4.25054 11.25 5.69624 11.25H6.30004C6.72008 11.25 6.9301 11.25 7.09053 11.3317C7.23166 11.4037 7.34639 11.5183 7.4183 11.6595C7.50008 11.8199 7.50008 12.0299 7.50008 12.45V14.6506C7.50008 15.672 8.328 16.5 9.34943 16.5C9.59303 16.5 9.81383 16.3565 9.91275 16.1339L12.4334 10.4626C12.548 10.2046 12.6053 10.0757 12.6959 9.98115C12.776 9.89752 12.8744 9.83362 12.9833 9.7944C13.1065 9.75 13.2476 9.75 13.5299 9.75H14.1001C14.9402 9.75 15.3602 9.75 15.681 9.5865C15.9633 9.44272 16.1927 9.21322 16.3366 8.931C16.5001 8.61015 16.5001 8.19007 16.5001 7.35Z"
    fill="currentColor"
  />
  <path
    d="M12.7501 1.5V9.75M16.5001 7.35V3.9C16.5001 3.05992 16.5001 2.63988 16.3366 2.31901C16.1927 2.03677 15.9633 1.8073 15.681 1.66349C15.3602 1.5 14.9402 1.5 14.1001 1.5H6.08855C4.99244 1.5 4.44438 1.5 4.00172 1.70057C3.61159 1.87736 3.28001 2.16181 3.04597 2.52054C2.78042 2.92754 2.69708 3.46923 2.53041 4.55259L2.1381 7.10259C1.91828 8.53147 1.80836 9.24592 2.0204 9.80182C2.20649 10.2898 2.55653 10.6978 3.01049 10.9559C3.52769 11.25 4.25054 11.25 5.69624 11.25H6.30004C6.72008 11.25 6.9301 11.25 7.09053 11.3317C7.23166 11.4037 7.34639 11.5183 7.4183 11.6595C7.50008 11.8199 7.50008 12.0299 7.50008 12.45V14.6506C7.50008 15.672 8.328 16.5 9.34943 16.5C9.59303 16.5 9.81383 16.3565 9.91275 16.1339L12.4334 10.4626C12.548 10.2046 12.6053 10.0757 12.6959 9.98115C12.776 9.89752 12.8744 9.83362 12.9833 9.7944C13.1065 9.75 13.2476 9.75 13.5299 9.75H14.1001C14.9402 9.75 15.3602 9.75 15.681 9.5865C15.9633 9.44272 16.1927 9.21322 16.3366 8.931C16.5001 8.61015 16.5001 8.19007 16.5001 7.35Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		thumbsDown: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M12.7501 1.5V9.75M16.5001 7.35V3.9C16.5001 3.05992 16.5001 2.63988 16.3366 2.31901C16.1927 2.03677 15.9633 1.8073 15.681 1.66349C15.3602 1.5 14.9402 1.5 14.1001 1.5H6.08855C4.99244 1.5 4.44438 1.5 4.00172 1.70057C3.61159 1.87736 3.28001 2.16181 3.04597 2.52054C2.78042 2.92754 2.69708 3.46923 2.53041 4.55259L2.1381 7.10259C1.91828 8.53147 1.80836 9.24592 2.0204 9.80182C2.20649 10.2898 2.55653 10.6978 3.01049 10.9559C3.52769 11.25 4.25054 11.25 5.69624 11.25H6.30004C6.72008 11.25 6.9301 11.25 7.09053 11.3317C7.23166 11.4037 7.34639 11.5183 7.4183 11.6595C7.50008 11.8199 7.50008 12.0299 7.50008 12.45V14.6506C7.50008 15.672 8.328 16.5 9.34943 16.5C9.59303 16.5 9.81383 16.3565 9.91275 16.1339L12.4334 10.4626C12.548 10.2046 12.6053 10.0757 12.6959 9.98115C12.776 9.89752 12.8744 9.83362 12.9833 9.7944C13.1065 9.75 13.2476 9.75 13.5299 9.75H14.1001C14.9402 9.75 15.3602 9.75 15.681 9.5865C15.9633 9.44272 16.1927 9.21322 16.3366 8.931C16.5001 8.61015 16.5001 8.19007 16.5001 7.35Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		thumbsUpAlt: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M5.25 16.5V8.25V16.5ZM1.5 9.75V15C1.5 15.8284 2.17157 16.5 3 16.5H13.0697C14.1803 16.5 15.1247 15.6898 15.2935 14.5921L16.1012 9.34215C16.3109 7.97917 15.2564 6.75 13.8774 6.75H11.25C10.8358 6.75 10.5 6.41421 10.5 6V3.34938C10.5 2.32799 9.672 1.5 8.65065 1.5C8.40697 1.5 8.18625 1.64348 8.08733 1.86609L5.44795 7.80457C5.32758 8.07547 5.05899 8.25 4.7626 8.25H3C2.17157 8.25 1.5 8.92155 1.5 9.75Z"
    fill="currentColor"
  />
  <path
    d="M5.25 16.5V8.25M1.5 9.75V15C1.5 15.8284 2.17157 16.5 3 16.5H13.0697C14.1803 16.5 15.1246 15.6898 15.2935 14.5921L16.1012 9.34215C16.3109 7.97917 15.2564 6.75 13.8774 6.75H11.25C10.8358 6.75 10.5 6.41421 10.5 6V3.34938C10.5 2.32799 9.672 1.5 8.65065 1.5C8.40697 1.5 8.18625 1.64348 8.08733 1.86609L5.44795 7.80457C5.32758 8.07547 5.05899 8.25 4.7626 8.25H3C2.17157 8.25 1.5 8.92155 1.5 9.75Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		thumbsUp: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M5.25 16.5V8.25M1.5 9.75V15C1.5 15.8284 2.17157 16.5 3 16.5H13.0697C14.1803 16.5 15.1246 15.6898 15.2935 14.5921L16.1012 9.34215C16.3109 7.97917 15.2564 6.75 13.8774 6.75H11.25C10.8358 6.75 10.5 6.41421 10.5 6V3.34938C10.5 2.32799 9.672 1.5 8.65065 1.5C8.40697 1.5 8.18625 1.64348 8.08733 1.86609L5.44795 7.80457C5.32758 8.07547 5.05899 8.25 4.7626 8.25H3C2.17157 8.25 1.5 8.92155 1.5 9.75Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		touchApp: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M7.85623 16.5C7.50623 16.5 7.1781 16.425 6.87185 16.275C6.5656 16.125 6.30623 15.9125 6.09373 15.6375L2.00623 10.4438L2.36248 10.0688C2.61248 9.80625 2.91248 9.65 3.26248 9.6C3.61248 9.55 3.93748 9.61875 4.23748 9.80625L5.62498 10.65V4.5C5.62498 4.2875 5.69685 4.10938 5.8406 3.96563C5.98435 3.82188 6.16248 3.75 6.37498 3.75C6.58748 3.75 6.76873 3.82188 6.91873 3.96563C7.06873 4.10938 7.14373 4.2875 7.14373 4.5V13.35L5.32498 12.225L7.27498 14.7188C7.34998 14.8063 7.43748 14.875 7.53748 14.925C7.63748 14.975 7.74373 15 7.85623 15H12C12.4125 15 12.7656 14.8531 13.0594 14.5594C13.3531 14.2656 13.5 13.9125 13.5 13.5V10.5C13.5 10.2875 13.4281 10.1094 13.2844 9.96563C13.1406 9.82188 12.9625 9.75 12.75 9.75H8.64373V8.25H12.75C13.375 8.25 13.9062 8.46875 14.3437 8.90625C14.7812 9.34375 15 9.875 15 10.5V13.5C15 14.325 14.7062 15.0313 14.1187 15.6188C13.5312 16.2063 12.825 16.5 12 16.5H7.85623ZM3.13123 6.375C2.96873 6.1 2.84373 5.80313 2.75623 5.48438C2.66873 5.16563 2.62498 4.8375 2.62498 4.5C2.62498 3.4625 2.9906 2.57813 3.72185 1.84688C4.4531 1.11563 5.33748 0.75 6.37498 0.75C7.41248 0.75 8.29685 1.11563 9.0281 1.84688C9.75935 2.57813 10.125 3.4625 10.125 4.5C10.125 4.8375 10.0812 5.16563 9.99373 5.48438C9.90623 5.80313 9.78123 6.1 9.61873 6.375L8.32498 5.625C8.42498 5.45 8.49998 5.27188 8.54998 5.09063C8.59998 4.90938 8.62498 4.7125 8.62498 4.5C8.62498 3.875 8.40623 3.34375 7.96873 2.90625C7.53123 2.46875 6.99998 2.25 6.37498 2.25C5.74998 2.25 5.21873 2.46875 4.78123 2.90625C4.34373 3.34375 4.12498 3.875 4.12498 4.5C4.12498 4.7125 4.14998 4.90938 4.19998 5.09063C4.24998 5.27188 4.32498 5.45 4.42498 5.625L3.13123 6.375Z"
    fill="currentColor"
  />
</svg>`,
		triangle: Q`
  <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M1.79264 13.5737L7.96133 2.91872C8.3022 2.32985 8.47268 2.03541 8.69513 1.93652C8.88908 1.85026 9.11055 1.85026 9.30458 1.93652C9.52695 2.03541 9.69743 2.32984 10.0383 2.91872L16.207 13.5737C16.5491 14.1647 16.7202 14.4602 16.6949 14.7027C16.6729 14.9142 16.562 15.1064 16.3901 15.2315C16.1928 15.3749 15.8514 15.3749 15.1685 15.3749H2.83115C2.14826 15.3749 1.80681 15.3749 1.6096 15.2315C1.43758 15.1064 1.32675 14.9142 1.30469 14.7027C1.27941 14.4602 1.45049 14.1647 1.79264 13.5737Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
</svg>`,
		type: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M3 5.25C3 4.55109 3 4.20164 3.11418 3.92597C3.26642 3.55844 3.55844 3.26642 3.92597 3.11418C4.20164 3 4.55109 3 5.25 3H12.75C13.4489 3 13.7984 3 14.0741 3.11418C14.4416 3.26642 14.7336 3.55844 14.8859 3.92597C15 4.20164 15 4.55109 15 5.25M6.75 15H11.25M9 3V15"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		unlock: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M5.25 8.25V6C5.25 3.92893 6.92894 2.25 9 2.25C10.8143 2.25 12.3275 3.53832 12.675 5.25M6.6 15.75H11.4C12.6602 15.75 13.2902 15.75 13.7715 15.5048C14.1949 15.2891 14.5391 14.9449 14.7548 14.5215C15 14.0401 15 13.4102 15 12.15V11.85C15 10.5899 15 9.95985 14.7548 9.4785C14.5391 9.05512 14.1949 8.71095 13.7715 8.49525C13.2902 8.25 12.6602 8.25 11.4 8.25H6.6C5.33988 8.25 4.70982 8.25 4.22852 8.49525C3.80516 8.71095 3.46095 9.05512 3.24524 9.4785C3 9.95985 3 10.5899 3 11.85V12.15C3 13.4102 3 14.0401 3.24524 14.5215C3.46095 14.9449 3.80516 15.2891 4.22852 15.5048C4.70982 15.75 5.33988 15.75 6.6 15.75Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		upload: Q`
  <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M15.75 11.25V12.15C15.75 13.4102 15.75 14.0401 15.5048 14.5215C15.2891 14.9449 14.9449 15.2891 14.5215 15.5048C14.0401 15.75 13.4102 15.75 12.15 15.75H5.85C4.58988 15.75 3.95982 15.75 3.47852 15.5048C3.05516 15.2891 2.71095 14.9449 2.49524 14.5215C2.25 14.0401 2.25 13.4102 2.25 12.15V11.25M12.75 6L9 2.25M9 2.25L5.25 6M9 2.25V11.25" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
</svg>`,
		user: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M15 15.75C15 14.7033 15 14.18 14.8708 13.7542C14.58 12.7954 13.8296 12.045 12.8708 11.7542C12.445 11.625 11.9217 11.625 10.875 11.625H7.125C6.07833 11.625 5.55499 11.625 5.12914 11.7542C4.17034 12.045 3.42003 12.7954 3.12918 13.7542C3 14.18 3 14.7033 3 15.75M12.375 5.625C12.375 7.48896 10.864 9 9 9C7.13604 9 5.625 7.48896 5.625 5.625C5.625 3.76104 7.13604 2.25 9 2.25C10.864 2.25 12.375 3.76104 12.375 5.625Z"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		userCircle: Q`
<svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
  <path d="M5.3163 19.4384C5.92462 18.0052 7.34492 17 9 17H15C16.6551 17 18.0754 18.0052 18.6837 19.4384M16 9.5C16 11.7091 14.2091 13.5 12 13.5C9.79086 13.5 8 11.7091 8 9.5C8 7.29086 9.79086 5.5 12 5.5C14.2091 5.5 16 7.29086 16 9.5ZM22 12C22 17.5228 17.5228 22 12 22C6.47715 22 2 17.5228 2 12C2 6.47715 6.47715 2 12 2C17.5228 2 22 6.47715 22 12Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
</svg>`,
		verticalCenter: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M12.75 4.5L5.25 4.5"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
  <path
    d="M3 9L15 9"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
  <path
    d="M6.75 13.5H11.25"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		verticalEnd: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M15 4.5L7.5 4.5"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
  <path
    d="M3 9L15 9"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
  <path
    d="M10.5 13.5H15"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		verticalStart: Q`
<svg
  width="18"
  height="18"
  viewBox="0 0 18 18"
  fill="none"
  xmlns="http://www.w3.org/2000/svg"
>
  <path
    d="M10.5 4.5L3 4.5"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
  <path
    d="M3 9L15 9"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
  <path
    d="M3 13.5H7.5"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>`,
		viewDay: Q`
<svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M3.75 12.75C3.3375 12.75 2.98438 12.6031 2.69063 12.3094C2.39687 12.0156 2.25 11.6625 2.25 11.25V6.75C2.25 6.3375 2.39687 5.98438 2.69063 5.69063C2.98438 5.39688 3.3375 5.25 3.75 5.25H14.25C14.6625 5.25 15.0156 5.39688 15.3094 5.69063C15.6031 5.98438 15.75 6.3375 15.75 6.75V11.25C15.75 11.6625 15.6031 12.0156 15.3094 12.3094C15.0156 12.6031 14.6625 12.75 14.25 12.75H3.75ZM3.75 11.25H14.25V6.75H3.75V11.25ZM2.25 3.75V2.25H15.75V3.75H2.25ZM2.25 15.75V14.25H15.75V15.75H2.25Z" fill="currentColor"/>
</svg>`,
		filePlus: Q`
  <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M15.0001 7.87504V5.10004C15.0001 3.83993 15.0001 3.20986 14.7548 2.72856C14.5391 2.3052 14.1949 1.961 13.7716 1.74528C13.2902 1.50004 12.6602 1.50004 11.4001 1.50004H6.60003C5.3399 1.50004 4.70985 1.50004 4.22855 1.74528C3.80518 1.961 3.46097 2.3052 3.24525 2.72856C3.00003 3.20986 3.00003 3.83993 3.00003 5.10004V12.9C3.00003 14.1601 3.00003 14.7902 3.24525 15.2715C3.46097 15.6949 3.80518 16.0391 4.22855 16.2548C4.70985 16.5 5.3399 16.5 6.60003 16.5H9.00002M13.5 15.75V11.25M11.25 13.5H15.75" 
  stroke="currentColor" 
  stroke-opacity="0.87" 
  stroke-width="1.5" 
  stroke-linecap="round" 
  stroke-linejoin="round"/>
</svg>


  `
	};
}));
//#endregion
export { Ci as $, ga as A, na as B, ja as C, wa as D, Da as E, _a as F, ra as G, $i as H, va as I, la as J, Ki as K, da as L, ha as M, ya as N, ba as O, fa as P, wi as Q, ua as R, Oa as S, Ea as T, Zi as U, Qi as V, ia as W, oa as X, sa as Y, aa as Z, Na as _, Go as a, xi as at, Ta as b, Uo as c, rn as ct, ro as d, zt as dt, Ei as et, oo as f, Pa as g, Ba as h, Jo as i, Vt as it, pa as j, ma as k, Ko as l, Nt as lt, Va as m, es as n, G as nt, $ as o, pn as ot, no as p, Gi as q, Qo as r, K as rt, jo as s, B as st, $o as t, Si as tt, Ao as u, Ut as ut, xa as v, Ca as w, ka as x, Sa as y, Ui as z };
