import { al as c } from "./copilot-Bm5k-h74.js";
/**
 * @license
 * Copyright 2020 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */
const { I: l } = c, d = (e) => e.strings === void 0, o = () => document.createComment(""), v = (e, i, t) => {
  const r = e._$AA.parentNode, $ = i === void 0 ? e._$AB : i._$AA;
  if (t === void 0) {
    const s = r.insertBefore(o(), $), _ = r.insertBefore(o(), $);
    t = new l(s, _, e, e.options);
  } else {
    const s = t._$AB.nextSibling, _ = t._$AM, n = _ !== e;
    if (n) {
      let A;
      t._$AQ?.(e), t._$AM = e, t._$AP !== void 0 && (A = e._$AU) !== _._$AU && t._$AP(A);
    }
    if (s !== $ || n) {
      let A = t._$AA;
      for (; A !== s; ) {
        const f = A.nextSibling;
        r.insertBefore(A, $), A = f;
      }
    }
  }
  return t;
}, B = (e, i, t = e) => (e._$AI(i, t), e), m = {}, g = (e, i = m) => e._$AH = i, p = (e) => e._$AH, x = (e) => {
  e._$AP?.(!1, !0);
  let i = e._$AA;
  const t = e._$AB.nextSibling;
  for (; i !== t; ) {
    const r = i.nextSibling;
    i.remove(), i = r;
  }
};
export {
  x as M,
  d as f,
  g as m,
  p,
  v as r,
  B as v
};
