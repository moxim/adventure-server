import { c as d, g as w, a as y, t as D, s as c, P as u, i as P, b as S, d as F, x as h, e as k, h as R, B as E, m as A, o as b, E as L, f as v, C as g, j as V, k as U } from "./copilot-Bm5k-h74.js";
import { i as m } from "./icons-mWh87_oB.js";
function l(o, t) {
  B(o) ? (D("show-in-ide", { attach: t ?? !1, goToCustomComponentFile: !0 }), c(`${u}show-in-ide`, {
    javaClassName: o.className,
    fileName: o.absoluteFilePath
  })) : P(o) ? (D("show-in-ide", { attach: t ?? !1 }), c(`${u}show-in-ide`, { ...S(o), attach: t ?? !1 })) : (F("show-in-ide"), c(`${u}show-in-ide`, o));
}
d.on("show-in-ide", (o) => {
  const t = o.detail.node;
  if (o.detail.source) {
    l(o.detail.source);
    return;
  }
  if (o.detail.javaSource) {
    l(o.detail.javaSource);
    return;
  }
  if (!t)
    return;
  if (t.isFlowComponent) {
    l(t.node, o.detail.attach);
    return;
  }
  const e = I(t);
  e && l(e);
});
function B(o) {
  return o === void 0 ? !1 : o.className !== void 0 ? !0 : o.absoluteFilePath !== void 0;
}
function I(o) {
  if (!o.isReactComponent)
    return;
  const t = w(o.node);
  if (t)
    return t;
  const e = y(o.node);
  if (e)
    return e;
  const n = o.children.sort((i, r) => i.siblingIndex - r.siblingIndex).find((i) => i.isReactComponent && I(i) !== void 0);
  if (!n)
    throw new Error(`Could not find the source of ${o.nameAndIdentifier}`);
  return w(n.node);
}
const f = "vaadin.copilot.viewCreated";
function p(o) {
  const t = document.createElement("div");
  document.body.innerHTML = "", document.body.appendChild(t), E(o, t);
}
function T() {
  p(
    h`<div class="flex flex-col gap-4 h-screen items-center justify-center">
      <vaadin-icon .svg=${m.rotatingSpinner}></vaadin-icon>
      <h3 class="m-0">The files have been created</h3>
      <p class="m-0">Restart the server to load the new view</p>
      <p class="m-0"><small>The page will refresh automatically when the server is ready.</small></p>
    </div>`
  );
}
async function N() {
  const e = Date.now(), n = async () => {
    try {
      return (await fetch(window.location.href, { method: "HEAD" })).ok;
    } catch {
      return !1;
    }
  };
  let i = !1;
  for (; Date.now() - e < 12e4; ) {
    if (!await n()) {
      i = !0;
      break;
    }
    await new Promise((a) => {
      setTimeout(a, 1e3);
    });
  }
  for (; Date.now() - e < 12e4; ) {
    if (await n() && i) {
      sessionStorage.removeItem(f), window.location.reload();
      return;
    }
    await new Promise((a) => {
      setTimeout(a, 1e3);
    });
  }
}
function x(o) {
  p(
    h`<div class="flex flex-col gap-4 h-screen items-center justify-center">
      <vaadin-icon .svg=${m.rotatingSpinner}></vaadin-icon>
      <h3 class="m-0">Creating your ${o === "flow" ? "Flow" : "Hilla"} view...</h3>
    </div>`
  ), k("copilot-init-app", { framework: o }, async (t) => {
    if (t.data.success)
      sessionStorage.setItem(f, "true"), T(), N();
    else {
      const e = t.data.reason;
      R(e);
    }
  });
}
function j() {
  p(
    h`<div class="m-8">
      <h3>No views found</h3>
      <p>To get started, you can</p>
      <ul>
        <li>
          <a
            href="#"
            @click=${(o) => {
      o.preventDefault(), x("flow");
    }}
            >Create a Flow view using Copilot</a
          >
        </li>
        <li>
          Create a view manually in your IDE, see
          <a target="_blank" href="https://vaadin.com/docs/latest/tutorial">the tutorial</a>
        </li>
      </ul>
      <p>Learn more at <a target="_blank" href="https://vaadin.com/docs">https://vaadin.com/docs</a>.</p>
    </div>`
  );
}
function $() {
  sessionStorage.getItem(f) ? (T(), N()) : j();
}
class W {
  constructor(t) {
    this._currentTree = t;
  }
  get root() {
    return this.currentTree.root;
  }
  get allNodesFlat() {
    return this.currentTree.allNodesFlat;
  }
  getNodeOfElement(t) {
    return this.currentTree.getNodeOfElement(t);
  }
  getChildren(t) {
    return this.currentTree.getChildren(t);
  }
  hasFlowComponents() {
    return this.currentTree.hasFlowComponents();
  }
  findNodeByUuid(t) {
    return this.currentTree.findNodeByUuid(t);
  }
  getElementByNodeUuid(t) {
    return this.currentTree.getElementByNodeUuid(t);
  }
  findByTreePath(t) {
    return this.currentTree.findByTreePath(t);
  }
  get currentTree() {
    return this._currentTree;
  }
  set currentTree(t) {
    this._currentTree = t, d.emit("copilot-tree-created", {});
  }
  get customComponentDataLoaded() {
    return this._currentTree.customComponentDataLoaded;
  }
}
d.on("navigate", (o) => {
  const t = window.history.state?.idx, e = {};
  t !== void 0 && (e.idx = t + 1), window.history.pushState(e, "", o.detail.path), window.dispatchEvent(new PopStateEvent("popstate"));
});
function O(o) {
  const t = window.Vaadin.copilot.tree;
  return o.map((e) => {
    let n = null;
    const { nodeUuid: i, treePath: r, childIndex: a } = e;
    if (i) {
      const C = t.findNodeByUuid(i);
      C && (n = C);
    }
    return n || (n = t.findByTreePath(r) ?? null), n && a !== void 0 && n.children.length > a ? n.children[a] : n;
  }).filter((e) => e !== null);
}
class s {
  constructor() {
    this.drillDownComponentStack = [], A(this, {
      drillDownComponentStack: b.shallow
    });
  }
  getCustomComponentIcon(t) {
    const e = this.getIconTag(t);
    return e === void 0 ? L : m[e];
  }
  getIconTag(t) {
    const n = this.getCustomComponentInfo(t)?.type;
    if (n === "IN_PROJECT")
      return "thermostatCarbon";
    if (n === "EXTERNAL")
      return "deployedCube";
  }
  getCustomComponentInfo(t) {
    if (t.customComponentData && s.isCustomComponentInstanceInfo(t.customComponentData))
      return t.customComponentData;
  }
  isCustomComponent(t) {
    return this.getCustomComponentInfo(t) !== void 0;
  }
  isVisibleAndSelectable(t) {
    if (!this.getTree().customComponentDataLoaded)
      return !0;
    const e = this.getActiveDrillDownContext();
    if (!t.customComponentData)
      return t.isReactComponent && !t.parent && t.name === "App" && !e;
    if (e?.uuid === t.uuid)
      return !1;
    const n = this.getActiveDrillDownData(), i = t.customComponentData;
    return n?.filePath ? t.customComponentData ? this.checkNodeIsInDrillDownContext(i, n) : !1 : i ? !i.childOfCustomComponent : !0;
  }
  pushDrillDownContext(t) {
    this.drillDownComponentStack.push(t), this.persistIntoStorage(), v(t);
  }
  isDrillDownContext(t) {
    return this.getActiveDrillDownContext()?.uuid === t.uuid;
  }
  getActiveDrillDownContext() {
    if (this.drillDownComponentStack.length !== 0)
      return this.drillDownComponentStack[this.drillDownComponentStack.length - 1];
  }
  clearDrillDownContext() {
    this.drillDownComponentStack = [], this.persistIntoStorage();
  }
  popDrillDownContext() {
    this.drillDownComponentStack.pop(), this.persistIntoStorage();
  }
  isChildInDrillContext(t) {
    const e = t.customComponentData;
    if (!e)
      return !0;
    const n = this.getActiveDrillDownData();
    return n ? this.checkNodeIsInDrillDownContext(e, n) : !1;
  }
  getActiveDrillDownData() {
    const t = this.getActiveDrillDownContext();
    if (t === void 0)
      return;
    const e = this.getCustomComponentInfo(t);
    if (!e?.javaClassName && !e?.reactMethodName)
      return;
    const n = t.node;
    return {
      className: e.javaClassName,
      methodName: e.reactMethodName,
      nodeId: n.nodeId,
      uiId: n.uiId,
      filePath: e.customComponentFilePath ?? void 0
    };
  }
  checkNodeIsInDrillDownContext(t, e) {
    return t.createLocationMethodName && e.methodName ? t.createLocationMethodName === e.methodName && e.filePath === t.createLocationPath : e.filePath === t.createLocationPath && e.className === t.createdClassName;
  }
  persistIntoStorage() {
    const t = this.drillDownComponentStack.map((e) => ({
      treePath: e.path,
      nodeUuid: e.uuid
    }));
    g.saveDrillDownContextReference(t);
  }
  restoreDrillDownFromStorage() {
    const t = g.getDrillDownContextReference();
    let e = [];
    if (t === void 0) {
      const i = this.getTree().allNodesFlat.find((r) => r.customComponentData?.routeView);
      i?.customComponentData && s.isCustomComponentInstanceInfo(i.customComponentData) && (e = [i]);
    } else
      e = O(t);
    e.forEach((i) => {
      const r = this.drillDownComponentStack.findIndex((a) => a.uuid === i.uuid);
      r !== -1 && this.drillDownComponentStack.splice(r, 1), this.drillDownComponentStack.push(i);
    });
    const n = this.getActiveDrillDownContext();
    n && v(n);
  }
  areInternalsVisible(t) {
    if (!this.getCustomComponentInfo(t))
      return !0;
    const n = this.getActiveDrillDownData();
    let i;
    return n && n.filePath && (i = n.filePath), i ? this.checkChildrenCreateLocationToDisplayInternals(t.children, i) : !1;
  }
  checkChildrenCreateLocationToDisplayInternals(t, e) {
    for (const n of t) {
      const i = n.customComponentData;
      if (i && i.createLocationPath === e || this.checkChildrenCreateLocationToDisplayInternals(n.children, e))
        return !0;
    }
    return !1;
  }
  getDescendantsCreatedInActiveDrillDownContextFlatten(t) {
    if (t.customComponentData && s.isCustomComponentInstanceInfo(t.customComponentData)) {
      const e = this.getActiveDrillDownData();
      let n;
      if (e && e.filePath ? n = e.filePath : this.getRouteViewPath() && (n = this.getRouteViewPath()), n)
        return this.getChildrenInPathFlattenRecursively(t, n);
    }
    return [];
  }
  getChildrenInPathFlattenRecursively(t, e) {
    const n = t.children, i = [];
    for (const r of n) {
      const a = r.customComponentData;
      a && a.createLocationPath === e && i.push(r), i.push(...this.getChildrenInPathFlattenRecursively(r, e));
    }
    return i;
  }
  /**
   * Accessed to copilot tree through window object to avoid circular dependency or initialization issues.
   * @private
   */
  getTree() {
    return window.Vaadin.copilot.tree;
  }
  getRouteViewPath() {
    const t = this.getTree().allNodesFlat.find((e) => e.customComponentData?.routeView === !0);
    if (t)
      return t.customComponentData?.createLocationPath ?? void 0;
  }
  static isCustomComponentInstanceInfo(t) {
    return "type" in t && "activeLevel" in t;
  }
}
window.Vaadin.copilot.comm = V;
const _ = new U();
window.Vaadin.copilot.tree = new W(_);
window.Vaadin.copilot.customComponentHandler = new s();
window.Vaadin.copilot.initEmptyApp = x;
window.Vaadin.copilot.noRoutesInProject = $;
