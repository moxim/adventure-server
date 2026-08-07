import { n as e } from "./chunk-DiqZc92J.js";
import { A as t, J as n, K as r, L as i, N as a, P as ee, R as o, _ as s, a as c, at as l, g as te, n as u, o as d, ot as f, q as p, r as m, st as h, t as g, u as _ } from "./icons-Daxdm7Mp.js";
import { l as v, o as y } from "./consts-PqkJ5Usg.js";
import { c as b, g as x } from "./dom-utils-U1Di7XbE.js";
import { a as S, n as C, o as w, t as T } from "./copilot-tree-impl-Bh3d7r5l.js";
import { n as E, t as D } from "./copilot-eventbus-tR06exIW.js";
import { i as O, n as k } from "./copilot-server-communicator-impl-DN0Hn9Ra.js";
import { a as A, i as j, t as M } from "./stats-D_j8yo6z.js";
import { i as N, o as P } from "./copilot-error-handler-B7az3wX2.js";
//#region frontend/copilot/show-in-ide.ts
function F(e, n) {
	I(e) ? (j("show-in-ide", {
		attach: n ?? !1,
		goToCustomComponentFile: !0
	}), s(`${y}show-in-ide`, {
		javaClassName: e.className,
		fileName: e.absoluteFilePath
	})) : ee(e) ? (j("show-in-ide", { attach: n ?? !1 }), s(`${y}show-in-ide`, {
		...t(e),
		attach: n ?? !1
	})) : (A("show-in-ide"), s(`${y}show-in-ide`, e));
}
function I(e) {
	return e === void 0 ? !1 : e.className === void 0 ? e.absoluteFilePath !== void 0 : !0;
}
function L(e) {
	if (!e.isReactComponent) return;
	let t = p(e.node);
	if (t) return t;
	let n = r(e.node);
	if (n) return n;
	let i = e.children.sort((e, t) => e.siblingIndex - t.siblingIndex).find((e) => e.isReactComponent && L(e) !== void 0);
	if (!i) throw Error(`Could not find the source of ${e.nameAndIdentifier}`);
	return p(i.node);
}
var R = e((() => {
	a(), n(), v(), E(), te(), M(), D.on("show-in-ide", (e) => {
		let t = e.detail.node;
		if (e.detail.source) {
			F(e.detail.source);
			return;
		}
		if (e.detail.javaSource) {
			F(e.detail.javaSource);
			return;
		}
		if (!t) return;
		if (t.isFlowComponent) {
			F(t.node, e.detail.attach);
			return;
		}
		let n = L(t);
		n && F(n);
	});
}));
//#endregion
//#region frontend/copilot/empty-app-initializer.ts
function z(e) {
	let t = document.createElement("div");
	document.body.innerHTML = "", document.body.appendChild(t), c(e, t);
}
function B() {
	z(_`<div class="flex flex-col gap-4 h-screen items-center justify-center">
      <vaadin-icon class="animate-spin" .svg=${g.progressActivity}></vaadin-icon>
      <h3 class="m-0">The files have been created</h3>
      <p class="m-0">Restart the server to load the new view</p>
      <p class="m-0"><small>The page will refresh automatically when the server is ready.</small></p>
    </div>`);
}
async function V() {
	let e = 1e3, t = 12e4, n = Date.now(), r = async () => {
		try {
			return (await fetch(globalThis.location.href, { method: "HEAD" })).ok;
		} catch {
			return !1;
		}
	}, i = !1;
	for (; Date.now() - n < t;) {
		if (!await r()) {
			i = !0;
			break;
		}
		await new Promise((t) => {
			setTimeout(t, e);
		});
	}
	for (; Date.now() - n < t;) {
		if (await r() && i) {
			sessionStorage.removeItem(G), globalThis.location.reload();
			return;
		}
		await new Promise((t) => {
			setTimeout(t, e);
		});
	}
}
function H(e) {
	z(_`<div class="flex flex-col gap-4 h-screen items-center justify-center">
      <vaadin-icon class="animate-spin" .svg=${g.progressActivity}></vaadin-icon>
      <h3 class="m-0">Creating your ${e === "flow" ? "Flow" : "Hilla"} view...</h3>
    </div>`), o("copilot-init-app", { framework: e }, async (e) => {
		if (e.data.success) sessionStorage.setItem(G, "true"), B(), V();
		else {
			let t = e.data.reason;
			N(t);
		}
	});
}
function U() {
	z(_`<div class="m-8">
      <h3>No views found</h3>
      <p>To get started, you can</p>
      <ul>
        <li>
          <a
            href="#"
            @click=${(e) => {
		e.preventDefault(), H("flow");
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
    </div>`);
}
function W() {
	sessionStorage.getItem(G) ? (B(), V()) : U();
}
var G, K = e((() => {
	i(), P(), u(), m(), G = "vaadin.copilot.viewCreated";
})), q, J = e((() => {
	E(), q = class {
		constructor(e) {
			this._currentTree = e;
		}
		get root() {
			return this.currentTree.root;
		}
		get allNodesFlat() {
			return this.currentTree.allNodesFlat;
		}
		getNodeOfElement(e) {
			return this.currentTree.getNodeOfElement(e);
		}
		getChildren(e) {
			return this.currentTree.getChildren(e);
		}
		hasFlowComponents() {
			return this.currentTree.hasFlowComponents();
		}
		findNodeByUuid(e) {
			return this.currentTree.findNodeByUuid(e);
		}
		getElementByNodeUuid(e) {
			return this.currentTree.getElementByNodeUuid(e);
		}
		findByTreePath(e) {
			return this.currentTree.findByTreePath(e);
		}
		get currentTree() {
			return this._currentTree;
		}
		set currentTree(e) {
			this._currentTree = e, D.emit("copilot-tree-created", {});
		}
		get customComponentDataLoaded() {
			return this._currentTree.customComponentDataLoaded;
		}
	};
})), Y = e((() => {
	E(), D.on("navigate", (e) => {
		let t = window.history.state?.idx, n = {};
		t !== void 0 && (n.idx = t + 1), window.history.pushState(n, "", e.detail.path), window.dispatchEvent(new PopStateEvent("popstate"));
	});
}));
//#endregion
//#region frontend/copilot/shared/copilot-storage-utils.ts
function X(e) {
	let t = window.Vaadin.copilot.tree;
	return e.map((e) => {
		let n = null, { nodeUuid: r, treePath: i, childIndex: a } = e;
		if (r) {
			let e = t.findNodeByUuid(r);
			e && (n = e);
		}
		return n ||= t.findByTreePath(i) ?? null, n && a !== void 0 && n.children.length > a ? n.children[a] : n;
	}).filter((e) => e !== null);
}
var Z = e((() => {})), Q, ne = e((() => {
	m(), u(), l(), w(), Z(), b(), Q = class e {
		constructor() {
			this.drillDownComponentStack = [], f(this, { drillDownComponentStack: h.shallow });
		}
		getCustomComponentIcon(e) {
			let t = this.getIconTag(e);
			return t === void 0 ? d : g[t];
		}
		getIconTag(e) {
			let t = this.getCustomComponentInfo(e)?.type;
			if (t === "IN_PROJECT") return "thermostatCarbon";
			if (t === "EXTERNAL") return "deployedCube";
		}
		getCustomComponentInfo(t) {
			if (t.customComponentData && e.isCustomComponentInstanceInfo(t.customComponentData)) return t.customComponentData;
		}
		isCustomComponent(e) {
			return this.getCustomComponentInfo(e) !== void 0;
		}
		isVisibleAndSelectable(e) {
			if (!this.getTree().customComponentDataLoaded) return !0;
			let t = this.getActiveDrillDownContext();
			if (!e.customComponentData) return e.isReactComponent && !e.parent && e.name === "App" && !t;
			if (e.uuid === t?.uuid) return !0;
			let n = this.getActiveDrillDownData(), r = e.customComponentData;
			if (!n?.filePath) {
				if (r) return !r.childOfCustomComponent;
			} else if (e.customComponentData) return this.checkNodeIsInDrillDownContext(r, n);
			else return !1;
			return !0;
		}
		pushDrillDownContext(e) {
			this.drillDownComponentStack.length > 0 && this.drillDownComponentStack[this.drillDownComponentStack.length - 1].uuid === e.uuid || (this.drillDownComponentStack.push(e), this.persistIntoStorage(), x(e));
		}
		isDrillDownContext(e) {
			return this.getActiveDrillDownContext()?.uuid === e.uuid;
		}
		getActiveDrillDownContext() {
			if (this.drillDownComponentStack.length !== 0) return this.resolveCurrentTreeNode(this.drillDownComponentStack[this.drillDownComponentStack.length - 1]);
		}
		clearDrillDownContext() {
			this.drillDownComponentStack = [], this.persistIntoStorage();
		}
		popDrillDownContext() {
			this.filterOutNonConnectedElementsFromDrillDownContextStack(), this.drillDownComponentStack.pop(), this.persistIntoStorage();
		}
		hasParentDrillDownContext() {
			return this.drillDownComponentStack.length > 1;
		}
		getParentDrillDownContext() {
			if (this.hasParentDrillDownContext()) return this.resolveCurrentTreeNode(this.drillDownComponentStack[this.drillDownComponentStack.length - 2]);
		}
		isChildInDrillContext(e) {
			let t = e.customComponentData;
			if (!t) return !0;
			let n = this.getActiveDrillDownData();
			return n ? this.checkNodeIsInDrillDownContext(t, n) : !1;
		}
		getActiveDrillDownData() {
			let e = this.getActiveDrillDownContext();
			if (e === void 0) return;
			let t = this.getCustomComponentInfo(e);
			if (!t?.javaClassName && !t?.reactMethodName) return;
			let n = e.node;
			return {
				className: t.javaClassName,
				methodName: t.reactMethodName,
				nodeId: n.nodeId,
				uiId: n.uiId,
				filePath: t.customComponentFilePath ?? void 0
			};
		}
		checkNodeIsInDrillDownContext(e, t) {
			return e.createLocationMethodName && t.methodName ? e.createLocationMethodName === t.methodName && t.filePath === e.createLocationPath : t.filePath === e.createLocationPath && t.className === e.createdClassName;
		}
		persistIntoStorage() {
			let e = this.drillDownComponentStack.map((e) => ({
				treePath: e.path,
				nodeUuid: e.uuid
			}));
			S.saveDrillDownContextReference(e);
		}
		restoreDrillDownFromStorage() {
			let t = S.getDrillDownContextReference(), n = [];
			if (t === void 0) {
				let t = this.getTree().allNodesFlat.find((e) => e.customComponentData?.routeView);
				t?.customComponentData && e.isCustomComponentInstanceInfo(t.customComponentData) && (n = [t]);
			} else n = X(t);
			n.forEach((e) => {
				let t = this.drillDownComponentStack.findIndex((t) => t.uuid === e.uuid);
				t !== -1 && this.drillDownComponentStack.splice(t, 1), this.drillDownComponentStack.push(e);
			});
			let r = this.drillDownComponentStack.filter((e) => !!this.getTree().findNodeByUuid(e.uuid));
			r.length !== this.drillDownComponentStack.length && (this.drillDownComponentStack = r, this.persistIntoStorage()), this.filterOutNonConnectedElementsFromDrillDownContextStack();
			let i = this.getActiveDrillDownContext();
			i && x(i);
		}
		areInternalsVisible(e) {
			if (!this.getCustomComponentInfo(e)) return !0;
			let t = this.getActiveDrillDownData(), n;
			return t && t.filePath && (n = t.filePath), n ? this.checkChildrenCreateLocationToDisplayInternals(e.children, n) : !1;
		}
		checkChildrenCreateLocationToDisplayInternals(e, t) {
			for (let n of e) {
				let e = n.customComponentData;
				if (e && e.createLocationPath === t || this.checkChildrenCreateLocationToDisplayInternals(n.children, t)) return !0;
			}
			return !1;
		}
		getDescendantsCreatedInActiveDrillDownContextFlatten(t) {
			if (t.customComponentData && e.isCustomComponentInstanceInfo(t.customComponentData)) {
				let e = this.getActiveDrillDownData(), n;
				if (e && e.filePath ? n = e.filePath : this.getRouteViewPath() && (n = this.getRouteViewPath()), n) return this.getChildrenInPathFlattenRecursively(t, n);
			}
			return [];
		}
		getChildrenInPathFlattenRecursively(e, t) {
			let n = e.children, r = [];
			for (let e of n) {
				let n = e.customComponentData;
				n && n.createLocationPath === t && r.push(e), r.push(...this.getChildrenInPathFlattenRecursively(e, t));
			}
			return r;
		}
		getTree() {
			return window.Vaadin.copilot.tree;
		}
		getRouteViewPath() {
			let e = this.getTree().allNodesFlat.find((e) => e.customComponentData?.routeView === !0);
			if (e) return e.customComponentData?.createLocationPath ?? void 0;
		}
		resolveCurrentTreeNode(e) {
			return this.getTree().findNodeByUuid(e.uuid) ?? this.getTree().findByTreePath(e.path) ?? e;
		}
		filterOutNonConnectedElementsFromDrillDownContextStack() {
			this.drillDownComponentStack = this.drillDownComponentStack.filter((e) => e.element === void 0 ? !0 : e.element.isConnected);
		}
		static isCustomComponentInstanceInfo(e) {
			return "type" in e && "activeLevel" in e;
		}
	};
})), $;
//#endregion
e((() => {
	R(), k(), K(), J(), Y(), C(), ne(), window.Vaadin.copilot.comm = O, $ = new T(), window.Vaadin.copilot.tree = new q($), window.Vaadin.copilot.customComponentHandler = new Q(), window.Vaadin.copilot.initEmptyApp = H, window.Vaadin.copilot.noRoutesInProject = W;
}))();
