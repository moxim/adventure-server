import { a5 as R, l as k, ac as S, ad as A, ae as U } from "./copilot-Bm5k-h74.js";
import { p as I, v, r as b, M as C, m as q } from "./directive-helpers-De0XYDBo.js";
var h, x;
function M() {
  return x || (x = 1, h = function() {
    var a = document.getSelection();
    if (!a.rangeCount)
      return function() {
      };
    for (var c = document.activeElement, d = [], f = 0; f < a.rangeCount; f++)
      d.push(a.getRangeAt(f));
    switch (c.tagName.toUpperCase()) {
      // .toUpperCase handles XHTML
      case "INPUT":
      case "TEXTAREA":
        c.blur();
        break;
      default:
        c = null;
        break;
    }
    return a.removeAllRanges(), function() {
      a.type === "Caret" && a.removeAllRanges(), a.rangeCount || d.forEach(function(r) {
        a.addRange(r);
      }), c && c.focus();
    };
  }), h;
}
var w, E;
function N() {
  if (E) return w;
  E = 1;
  var a = M(), c = {
    "text/plain": "Text",
    "text/html": "Url",
    default: "Text"
  }, d = "Copy to clipboard: #{key}, Enter";
  function f(l) {
    var t = (/mac os x/i.test(navigator.userAgent) ? "⌘" : "Ctrl") + "+C";
    return l.replace(/#{\s*key\s*}/g, t);
  }
  function r(l, t) {
    var i, p, y, m, o, e, n = !1;
    t || (t = {}), i = t.debug || !1;
    try {
      y = a(), m = document.createRange(), o = document.getSelection(), e = document.createElement("span"), e.textContent = l, e.ariaHidden = "true", e.style.all = "unset", e.style.position = "fixed", e.style.top = 0, e.style.clip = "rect(0, 0, 0, 0)", e.style.whiteSpace = "pre", e.style.webkitUserSelect = "text", e.style.MozUserSelect = "text", e.style.msUserSelect = "text", e.style.userSelect = "text", e.addEventListener("copy", function(s) {
        if (s.stopPropagation(), t.format)
          if (s.preventDefault(), typeof s.clipboardData > "u") {
            i && console.warn("unable to use e.clipboardData"), i && console.warn("trying IE specific stuff"), window.clipboardData.clearData();
            var g = c[t.format] || c.default;
            window.clipboardData.setData(g, l);
          } else
            s.clipboardData.clearData(), s.clipboardData.setData(t.format, l);
        t.onCopy && (s.preventDefault(), t.onCopy(s.clipboardData));
      }), document.body.appendChild(e), m.selectNodeContents(e), o.addRange(m);
      var u = document.execCommand("copy");
      if (!u)
        throw new Error("copy command was unsuccessful");
      n = !0;
    } catch (s) {
      i && console.error("unable to copy using execCommand: ", s), i && console.warn("trying IE specific stuff");
      try {
        window.clipboardData.setData(t.format || "text", l), t.onCopy && t.onCopy(window.clipboardData), n = !0;
      } catch (g) {
        i && console.error("unable to copy using clipboardData: ", g), i && console.error("falling back to prompt"), p = f("message" in t ? t.message : d), window.prompt(p, l);
      }
    } finally {
      o && (typeof o.removeRange == "function" ? o.removeRange(m) : o.removeAllRanges()), e && document.body.removeChild(e), y();
    }
    return n;
  }
  return w = r, w;
}
var P = N();
const F = /* @__PURE__ */ R(P);
/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */
const T = (a, c, d) => {
  const f = /* @__PURE__ */ new Map();
  for (let r = c; r <= d; r++) f.set(a[r], r);
  return f;
}, H = k(class extends S {
  constructor(a) {
    if (super(a), a.type !== A.CHILD) throw Error("repeat() can only be used in text expressions");
  }
  dt(a, c, d) {
    let f;
    d === void 0 ? d = c : c !== void 0 && (f = c);
    const r = [], l = [];
    let t = 0;
    for (const i of a) r[t] = f ? f(i, t) : t, l[t] = d(i, t), t++;
    return { values: l, keys: r };
  }
  render(a, c, d) {
    return this.dt(a, c, d).values;
  }
  update(a, [c, d, f]) {
    const r = I(a), { values: l, keys: t } = this.dt(c, d, f);
    if (!Array.isArray(r)) return this.ut = t, l;
    const i = this.ut ??= [], p = [];
    let y, m, o = 0, e = r.length - 1, n = 0, u = l.length - 1;
    for (; o <= e && n <= u; ) if (r[o] === null) o++;
    else if (r[e] === null) e--;
    else if (i[o] === t[n]) p[n] = v(r[o], l[n]), o++, n++;
    else if (i[e] === t[u]) p[u] = v(r[e], l[u]), e--, u--;
    else if (i[o] === t[u]) p[u] = v(r[o], l[u]), b(a, p[u + 1], r[o]), o++, u--;
    else if (i[e] === t[n]) p[n] = v(r[e], l[n]), b(a, r[o], r[e]), e--, n++;
    else if (y === void 0 && (y = T(t, n, u), m = T(i, o, e)), y.has(i[o])) if (y.has(i[e])) {
      const s = m.get(t[n]), g = s !== void 0 ? r[s] : null;
      if (g === null) {
        const D = b(a, r[o]);
        v(D, l[n]), p[n] = D;
      } else p[n] = v(g, l[n]), b(a, r[o], g), r[s] = null;
      n++;
    } else C(r[e]), e--;
    else C(r[o]), o++;
    for (; n <= u; ) {
      const s = b(a, p[u + 1]);
      v(s, l[n]), p[n++] = s;
    }
    for (; o <= e; ) {
      const s = r[o++];
      s !== null && C(s);
    }
    return this.ut = t, q(a, p), U;
  }
});
export {
  F as a,
  H as c
};
