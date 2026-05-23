import { l as sn, n as Le, M as _t, p as M, q as O, O as Oi, A as Ut, r as aa, E as Oe, u as Xt, x as C, v as oa, c as X, w as sa, y as ln, e as Ai, z as Pe, D as se, F as la, G as ca, H as Pi, I as da, J as ua, K as Lt, L as ha, N as K, Q as cn, R as fa, S as pa, T as Te, U as Mt, V as Nt, W as In, X as ma, Y as ga, Z as va, _ as st, $ as je, a0 as dn, C as Wt, a1 as ba, a2 as Ea, a3 as ya, a4 as wa, a5 as Ra, P as Ia } from "./copilot-Bm5k-h74.js";
import { r as ve } from "./state-BH_9RcTJ.js";
import { c as et, a as _i } from "./repeat-xjtCsDSL.js";
import { L as Sa } from "./lit-renderer-DHSDc4RC.js";
import { i as ee } from "./icons-mWh87_oB.js";
/**
 * @license
 * Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
 */
let Sn = 0, Li = 0;
const Ue = [];
let Yr = !1;
function $a() {
  Yr = !1;
  const t = Ue.length;
  for (let e = 0; e < t; e++) {
    const r = Ue[e];
    if (r)
      try {
        r();
      } catch (n) {
        setTimeout(() => {
          throw n;
        });
      }
  }
  Ue.splice(0, t), Li += t;
}
const Ta = {
  /**
   * Enqueues a function called at microtask timing.
   *
   * @memberof microTask
   * @param {!Function=} callback Callback to run
   * @return {number} Handle used for canceling task
   */
  run(t) {
    Yr || (Yr = !0, queueMicrotask(() => $a())), Ue.push(t);
    const e = Sn;
    return Sn += 1, e;
  },
  /**
   * Cancels a previously enqueued `microTask` callback.
   *
   * @memberof microTask
   * @param {number} handle Handle returned from `run` of callback to cancel
   * @return {void}
   */
  cancel(t) {
    const e = t - Li;
    if (e >= 0) {
      if (!Ue[e])
        throw new Error(`invalid async handle: ${t}`);
      Ue[e] = null;
    }
  }
};
/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/
const $n = /* @__PURE__ */ new Set();
class St {
  /**
   * Creates a debouncer if no debouncer is passed as a parameter
   * or it cancels an active debouncer otherwise. The following
   * example shows how a debouncer can be called multiple times within a
   * microtask and "debounced" such that the provided callback function is
   * called once. Add this method to a custom element:
   *
   * ```js
   * import {microTask} from '@vaadin/component-base/src/async.js';
   * import {Debouncer} from '@vaadin/component-base/src/debounce.js';
   * // ...
   *
   * _debounceWork() {
   *   this._debounceJob = Debouncer.debounce(this._debounceJob,
   *       microTask, () => this._doWork());
   * }
   * ```
   *
   * If the `_debounceWork` method is called multiple times within the same
   * microtask, the `_doWork` function will be called only once at the next
   * microtask checkpoint.
   *
   * Note: In testing it is often convenient to avoid asynchrony. To accomplish
   * this with a debouncer, you can use `enqueueDebouncer` and
   * `flush`. For example, extend the above example by adding
   * `enqueueDebouncer(this._debounceJob)` at the end of the
   * `_debounceWork` method. Then in a test, call `flush` to ensure
   * the debouncer has completed.
   *
   * @param {Debouncer?} debouncer Debouncer object.
   * @param {!AsyncInterface} asyncModule Object with Async interface
   * @param {function()} callback Callback to run.
   * @return {!Debouncer} Returns a debouncer object.
   */
  static debounce(e, r, n) {
    return e instanceof St ? e._cancelAsync() : e = new St(), e.setConfig(r, n), e;
  }
  constructor() {
    this._asyncModule = null, this._callback = null, this._timer = null;
  }
  /**
   * Sets the scheduler; that is, a module with the Async interface,
   * a callback and optional arguments to be passed to the run function
   * from the async module.
   *
   * @param {!AsyncInterface} asyncModule Object with Async interface.
   * @param {function()} callback Callback to run.
   * @return {void}
   */
  setConfig(e, r) {
    this._asyncModule = e, this._callback = r, this._timer = this._asyncModule.run(() => {
      this._timer = null, $n.delete(this), this._callback();
    });
  }
  /**
   * Cancels an active debouncer and returns a reference to itself.
   *
   * @return {void}
   */
  cancel() {
    this.isActive() && (this._cancelAsync(), $n.delete(this));
  }
  /**
   * Cancels a debouncer's async callback.
   *
   * @return {void}
   */
  _cancelAsync() {
    this.isActive() && (this._asyncModule.cancel(
      /** @type {number} */
      this._timer
    ), this._timer = null);
  }
  /**
   * Flushes an active debouncer and returns a reference to itself.
   *
   * @return {void}
   */
  flush() {
    this.isActive() && (this.cancel(), this._callback());
  }
  /**
   * Returns true if the debouncer is active.
   *
   * @return {boolean} True if active.
   */
  isActive() {
    return this._timer != null;
  }
}
/**
 * @license
 * Copyright (c) 2017 - 2025 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */
const Yt = Symbol("contentUpdateDebouncer");
class un extends Sa {
  /**
   * A property to that the renderer callback will be assigned.
   *
   * @abstract
   */
  get rendererProperty() {
    throw new Error("The `rendererProperty` getter must be implemented.");
  }
  /**
   * Adds the renderer callback to the dialog.
   */
  addRenderer() {
    this.element[this.rendererProperty] = (e, r) => {
      this.renderRenderer(e, r);
    };
  }
  /**
   * Runs the renderer callback on the dialog.
   */
  runRenderer() {
    this.element[Yt] = St.debounce(
      this.element[Yt],
      Ta,
      () => {
        this.element.requestContentUpdate();
      }
    );
  }
  /**
   * Removes the renderer callback from the dialog.
   */
  removeRenderer() {
    this.element[this.rendererProperty] = null, delete this.element[Yt];
  }
}
class xa extends un {
  get rendererProperty() {
    return "renderer";
  }
}
class Ca extends un {
  get rendererProperty() {
    return "headerRenderer";
  }
}
class Da extends un {
  get rendererProperty() {
    return "footerRenderer";
  }
}
const Ht = sn(xa), kt = sn(Ca), Ft = sn(Da);
var Oa = Object.defineProperty, Aa = Object.getOwnPropertyDescriptor, hn = (t, e, r, n) => {
  for (var i = n > 1 ? void 0 : n ? Aa(e, r) : e, a = t.length - 1, o; a >= 0; a--)
    (o = t[a]) && (i = (n ? o(e, r, i) : o(i)) || i);
  return n && i && Oa(e, r, i), i;
};
let $t = class extends _t {
  constructor() {
    super(...arguments), this.panelHeaderUpdated = 0, this.openedPanelConfigurationsSorted = [], this.windowResizeListener = async () => {
      await Promise.all(
        this.getOpenPanelContainerDialogs().map(async (t) => {
          const e = t.getAttribute("data-panel-tag");
          if (!e)
            return;
          await t.querySelector(e)?.requestLayoutUpdate();
          const n = M.getPanelByTag(e);
          if (!n?.position)
            return;
          const i = t.getBoundingClientRect(), a = this.getViewportAdjustedPosition({
            ...n.position,
            width: n.position.width ?? i.width,
            height: n.position.height ?? i.height
          });
          t.setAttribute("top", `${a.top}`), t.setAttribute("left", `${a.left}`), t.setAttribute("width", `${a.width}`), t.setAttribute("height", `${a.height}`), M.updatePanel(
            n.tag,
            {
              position: a
            },
            !1
          );
        })
      );
    };
  }
  connectedCallback() {
    super.connectedCallback(), window.addEventListener("resize", this.windowResizeListener), this.reaction(
      () => O.operationInProgress,
      () => {
        Array.from(this.querySelectorAll("vaadin-dialog[panel-container]")).forEach((t) => {
          const e = t.hasAttribute("showWhileDragging");
          requestAnimationFrame(() => {
            t.toggleAttribute(
              "hiding-while-drag-and-drop",
              O.operationInProgress === Oi.DragAndDrop && !e && !this.hasDropTarget(t)
            );
          });
        });
      }
    ), this.reaction(
      () => M.getAttentionRequiredPanelConfiguration(),
      () => {
        const t = M.getAttentionRequiredPanelConfiguration(), e = this.getDialogByPanelTag(t?.tag);
        e && e.toggleAttribute(Ut, !0);
      }
    ), this.observeDisposer = aa(M.getCustomPanelHeaderMap(), () => {
      this.panelHeaderUpdated += 1;
    }), this.reaction(
      () => [
        M.panels,
        M.panelStackingOrder,
        M.getOpenPanelTags().size
      ],
      () => {
        const t = M.panels.filter((r) => M.isOpenedPanel(r.tag)), e = M.panelStackingOrder;
        this.openedPanelConfigurationsSorted = t.sort((r, n) => {
          const i = e.indexOf(r.tag), a = e.indexOf(n.tag);
          return i - a;
        });
      },
      { fireImmediately: !0 }
    );
  }
  disconnectedCallback() {
    super.disconnectedCallback(), window.removeEventListener("resize", this.windowResizeListener), this.observeDisposer && this.observeDisposer();
  }
  createRenderRoot() {
    return this;
  }
  render() {
    return C`
      ${et(
      this.openedPanelConfigurationsSorted,
      (t) => t.tag,
      (t) => C`<vaadin-dialog
            draggable
            modeless
            id="${t.tag}-dialog"
            waits-positioning
            ?individual="${t.individual}"
            .opened="${M.isOpenedPanel(t.tag)}"
            panel-container
            resizable
            @mousedown="${(e) => {
        M.moveStackingOrderToTop(t.tag);
      }}"
            @touchstart="${(e) => {
        M.moveStackingOrderToTop(t.tag);
      }}"
            data-panel-tag="${t.tag}"
            ?showWhileDragging="${t.showWhileDragging}"
            theme="no-padding"
            left="${t.position?.left}"
            top="${t.position?.top}"
            height=${t.position?.height}
            width=${t.position?.width}
            @resize="${(e) => {
        const { top: r, left: n, width: i, height: a } = e.detail, o = e.currentTarget, d = Number.parseFloat(i), c = Number.parseFloat(a), s = this.getViewportAdjustedPosition({
          top: Number.parseFloat(r),
          left: Number.parseFloat(n),
          width: Number.isFinite(d) ? d : o.getBoundingClientRect().width,
          height: Number.isFinite(c) ? c : o.getBoundingClientRect().height
        });
        o.setAttribute("top", `${s.top}`), o.setAttribute("left", `${s.left}`), o.setAttribute("width", `${s.width}`), o.setAttribute("height", `${s.height}`), M.updatePanel(t.tag, {
          position: s
        });
      }}"
            @dragged="${(e) => {
        const r = M.getPanelByTag(t.tag);
        if (!r)
          return;
        const n = e.currentTarget, i = n.getBoundingClientRect(), a = {
          top: Number.parseFloat(e.detail.top),
          left: Number.parseFloat(e.detail.left)
        }, o = this.getViewportAdjustedPosition({
          width: i.width,
          height: i.height,
          ...r.position,
          ...a
        });
        n.setAttribute("top", `${o.top}`), n.setAttribute("left", `${o.left}`), M.updatePanel(r.tag, {
          position: o
        });
      }}"
            @mouseenter="${(e) => {
        const r = e.target;
        r.hasAttribute(Ut) && M.clearAttention(), r.toggleAttribute(Ut, !1);
      }}"
            @opened-changed="${(e) => {
        const r = e.detail.value, n = e.currentTarget;
        r && (M.moveStackingOrderToTop(t.tag), (n.querySelector(t.tag)?.requestLayoutUpdate() ?? Promise.resolve()).finally(() => {
          setTimeout(() => {
            n.removeAttribute("waits-positioning");
          }, 50);
        }));
        const i = this.getToolbar();
        if (!i)
          return;
        const a = i.getButtonId(t), o = i.querySelector(`vaadin-button#${a}`);
        o && (r ? o.classList.add("selected") : o.classList.remove("selected"));
      }}"
            ${kt(
        () => C`
                <h2 class="font-bold me-auto my-0 text-xs truncate uppercase draggable" id="${t.tag}-title">
                  ${M.getPanelHeader(t)}
                  ${t.experimental ? C`<vaadin-icon slot="suffix" .svg="${ee.experiment}"></vaadin-icon>
                        <vaadin-tooltip slot="tooltip" text="Experimental feature"></vaadin-tooltip>` : Oe}
                </h2>
                ${t.actionsTag ? Xt(`<${t.actionsTag}></${t.actionsTag}>`) : Oe}
                ${t.helpUrl ? C`<vaadin-button
                      aria-label="More info about ${t.header}"
                      @click="${() => window.open(t.helpUrl, "_blank")}"
                      @mousedown="${(e) => e.stopPropagation()}"
                      theme="icon tertiary">
                      <vaadin-icon .svg="${ee.help}"></vaadin-icon>
                      <vaadin-tooltip slot="tooltip" text="More info about ${t.header}"></vaadin-tooltip>
                    </vaadin-button>` : Oe}
                <vaadin-button
                  aria-label="Close"
                  @click="${() => {
          const r = this.getDialogByPanelTag(t.tag)?.querySelector(t.tag);
          r?.requestClose ? r.requestClose(() => M.closePanel(t.tag)) : M.closePanel(t.tag);
        }}"
                  @mousedown="${(e) => e.stopPropagation()}"
                  theme="icon tertiary">
                  <vaadin-icon .svg="${ee.close}"></vaadin-icon>
                  <vaadin-tooltip slot="tooltip" text="Close"></vaadin-tooltip>
                </vaadin-button>
              `,
        [this.panelHeaderUpdated]
      )}
            ${Ht(
        () => C`<div class="w-full h-full">${Xt(`<${t.tag}></${t.tag}>`)}</div>`,
        []
      )}
            ${t.footerActionsTag ? Ft(
        () => C`<div>
                      ${Xt(`<${t.footerActionsTag}></${t.footerActionsTag}>`)}
                    </div>`,
        []
      ) : Oe}></vaadin-dialog>`
    )}
    `;
  }
  hasDropTarget(t) {
    const e = t.children;
    for (const r of e) {
      const n = oa(r.shadowRoot ?? r, "copilot-image-upload");
      if (n && getComputedStyle(n).display !== "none")
        return !0;
    }
    return !1;
  }
  /**
   * Queries open panel dialogs. This method returns sorted list of dialogs based on their stack orders
   */
  getOpenPanelContainerDialogs() {
    const t = Array.from(this.querySelectorAll("vaadin-dialog[panel-container][opened]")), e = M.panelStackingOrder;
    return t.sort((r, n) => {
      const i = r.dataset.panelTag, a = n.dataset.panelTag;
      return i === null ? a === null ? 0 : 1 : a === null ? -1 : e.indexOf(i) - e.indexOf(a);
    }), t;
  }
  getDialogByPanelTag(t) {
    return t ? this.querySelector(`vaadin-dialog#${t}-dialog`) : null;
  }
  getToolbar() {
    const t = document.querySelector("copilot-main");
    return t ? t.shadowRoot.querySelector("copilot-toolbar") : null;
  }
  /**
   * Adjusts panel position to keep it within the viewport by applying edge offsets.
   *
   * The method preserves the given width and height and only shifts `top`/`left`:
   * - If left/top are negative, it moves the panel back to 0 on that axis.
   * - If right/bottom exceed viewport bounds minus padding, it moves the panel inward by the overflow amount with additional padding
   *
   * No left/top custom padding, min-size, or max-size constraints are applied.
   */
  getViewportAdjustedPosition(t) {
    const e = this.getViewportPaddingPx(), r = Number.isFinite(t.width) ? t.width : 0, n = Number.isFinite(t.height) ? t.height : 0;
    let i = Number.isFinite(t.top) ? t.top : 0, a = Number.isFinite(t.left) ? t.left : 0;
    const o = a + r, d = i + n;
    let c = 0, s = 0;
    return a < 0 ? c = -a : o > window.innerWidth - e && (c = window.innerWidth - e - o), i < 0 ? s = -i : d > window.innerHeight - e && (s = window.innerHeight - e - d), a += c, i += s, { top: i, left: a, width: r, height: n };
  }
  getViewportPaddingPx() {
    const t = Number.parseFloat(getComputedStyle(this).fontSize);
    return Number.isFinite(t) ? t : 16;
  }
};
hn([
  ve()
], $t.prototype, "panelHeaderUpdated", 2);
hn([
  ve()
], $t.prototype, "openedPanelConfigurationsSorted", 2);
$t = hn([
  Le("copilot-panel-manager")
], $t);
const zr = window.Vaadin.copilot.customComponentHandler;
if (!zr)
  throw new Error("Tried to access custom component handler before it was initialized.");
function Pa(t) {
  O.setOperationWaitsHmrUpdate(t, 3e4);
}
X.on("undoRedo", (t) => {
  const r = { files: _a(t), uiId: sa() }, n = t.detail.undo ? "copilot-plugin-undo" : "copilot-plugin-redo", i = t.detail.undo ? "undo" : "redo";
  ln(i), Pa(Oi.RedoUndo), Ai(n, r, (a) => {
    if (!a.data.performed) {
      if (a.data.error && a.data.error.message) {
        Pe({
          type: se.ERROR,
          message: a.data.error.message
        }), X.emit("vite-after-update", {});
        return;
      }
      Pe({
        type: se.INFORMATION,
        message: `Nothing to ${i}`
      }), X.emit("vite-after-update", {});
    }
  });
});
function _a(t) {
  if (t.detail.files)
    return t.detail.files;
  const e = zr.getActiveDrillDownContext();
  if (e) {
    const r = zr.getCustomComponentInfo(e);
    if (r)
      return new Array(r.customComponentFilePath);
  }
  return la();
}
var La = Object.getOwnPropertyDescriptor, Ma = (t, e, r, n) => {
  for (var i = n > 1 ? void 0 : n ? La(e, r) : e, a = t.length - 1, o; a >= 0; a--)
    (o = t[a]) && (i = o(i) || i);
  return i;
};
let Tn = class extends _t {
  createRenderRoot() {
    return this;
  }
  connectedCallback() {
    super.connectedCallback(), this.classList.add("end-2", "fixed", "top-2", "z-100"), X.on("show-notification", (t) => {
      const e = t.detail.notification;
      Pe(e);
    });
  }
  /**
   * Notifications with interactive elements are problematic; we should consider alternatives (i.e. alertdialog).
   */
  render() {
    return C`<section aria-label="Notifications" aria-live="polite" class="flex flex-col-reverse list-none m-0 p-0">
      ${et(
      O.notifications,
      (t) => t.id,
      (t) => this.renderNotification(t)
    )}
    </section>`;
  }
  renderNotification(t) {
    return C`
      <div
        class="duration-300 origin-top transition-all ${t.animatingIn ? "delay-300 max-h-0 opacity-0 scale-99 -translate-y-2" : t.animatingOut ? "delay-0 max-h-0 opacity-0 scale-99 -translate-y-2" : "max-h-screen"}"
        data-testid="message"
        ?data-error="${t.type === se.ERROR}">
        <div class="bg-gray-1 dark:bg-gray-5 border flex gap-2 mb-2 p-3 relative rounded-md shadow-xl w-sm">
          <vaadin-icon
            class="${t.type === se.ERROR ? "text-ruby-11" : t.type === se.WARNING ? "text-amber-11" : "text-blue-11"}"
            .svg="${t.type === se.WARNING || t.type === se.ERROR ? ee.warning : ee.info}"></vaadin-icon>
          <div class="flex flex-col flex-1">
            <h2 class="m-0 text-sm">${t.message}</h2>
            <div
              class="break-word flex flex-col items-start text-secondary text-xs"
              ?hidden="${!t.details && !t.link}">
              ${ca(t.details)}
              ${t.link ? C`<a class="ahreflike" href="${t.link}" target="_blank">Learn more</a>` : ""}
            </div>
            ${t.dismissId ? C` <hr class="border-b border-e-0 border-s-0 border-t-0 mx-0 my-3 w-full" />
                  <vaadin-checkbox
                    ?checked=${t.dontShowAgain}
                    @change=${() => this.toggleDontShowAgain(t)}
                    label="${Na(t)}">
                  </vaadin-checkbox>` : ""}
          </div>
          <vaadin-button
            aria-label="Close"
            class="absolute end-1.5 top-1.5"
            @click=${(e) => {
      Pi(t), e.stopPropagation();
    }}
            id="dismiss"
            theme="icon tertiary">
            <vaadin-icon .svg="${ee.close}"></vaadin-icon>
            <vaadin-tooltip slot="tooltip" text="Close"></vaadin-tooltip>
          </vaadin-button>
        </div>
      </div>
    `;
  }
  toggleDontShowAgain(t) {
    t.dontShowAgain = !t.dontShowAgain, this.requestUpdate();
  }
};
Tn = Ma([
  Le("copilot-notifications-container")
], Tn);
function Na(t) {
  return t.dontShowAgainMessage ? t.dontShowAgainMessage : "Don't show again";
}
Pe({
  type: se.WARNING,
  message: "Development Mode",
  details: "This application is running in development mode.",
  dismissId: "devmode"
});
const Jr = da(async () => {
  await ua();
}, 100);
X.on("vite-after-update", () => {
  Lt().default || Jr();
});
function Mi() {
  Lt().default || (Jr.clear(), Jr(), pa());
}
if (window.__REACT_DEVTOOLS_GLOBAL_HOOK__) {
  const t = window.__REACT_DEVTOOLS_GLOBAL_HOOK__, e = t.onCommitFiberRoot;
  t.onCommitFiberRoot = (r, n, i, a) => (Mi(), e(r, n, i, a));
}
ha(() => {
  const t = window?.Vaadin?.connectionState;
  if (t)
    return t;
}).then((t) => {
  t.addStateChangeListener && typeof t.addStateChangeListener == "function" ? t.addStateChangeListener((e, r) => {
    e === "loading" && r === "connected" && !Lt().default && Mi();
  }) : console.error("Unable to add listener for connection state changes");
});
X.on("copilot-plugin-state", (t) => {
  O.setIdePluginState(t.detail), t.preventDefault();
});
X.on("copilot-early-project-state", (t) => {
  K.setSpringSecurityEnabled(t.detail.springSecurityEnabled), K.setSpringJpaDataEnabled(t.detail.springJpaDataEnabled), K.setSpringJpaDatasourceInitialization(t.detail.springJpaDatasourceInitialization), K.setSupportsHilla(t.detail.supportsHilla), K.setSpringApplication(t.detail.springApplication), K.setUrlPrefix(t.detail.urlPrefix), K.setServerVersions(t.detail.serverVersions), K.setJdkInfo(t.detail.jdkInfo), cn() === "success" && ln("hotswap-active", { value: fa() }), t.preventDefault();
});
X.on("copilot-ide-notification", (t) => {
  Pe({
    type: se[t.detail.type],
    message: t.detail.message,
    dismissId: t.detail.dismissId
  }), t.preventDefault();
});
var Ha = Object.defineProperty, ka = Object.getOwnPropertyDescriptor, Ni = (t, e, r, n) => {
  for (var i = n > 1 ? void 0 : n ? ka(e, r) : e, a = t.length - 1, o; a >= 0; a--)
    (o = t[a]) && (i = (n ? o(e, r, i) : o(i)) || i);
  return n && i && Ha(e, r, i), i;
};
let Kr = class extends Mt {
  constructor() {
    super(...arguments), this.rememberChoice = !1, this.opened = !1, this.handleESC = (t) => {
      Lt().appInteractable || !this.opened || (t.key === "Escape" && this.sendEvent("cancel"), t.preventDefault(), t.stopPropagation());
    };
  }
  createRenderRoot() {
    return this;
  }
  connectedCallback() {
    super.connectedCallback(), this.addESCListener();
  }
  disconnectedCallback() {
    super.disconnectedCallback(), this.removeESCListener();
  }
  render() {
    return C` <vaadin-dialog
      id="ai-dialog"
      no-close-on-outside-click
      class="ai-dialog"
      ?opened=${this.opened}
      ${kt(
      () => C`
          <h2>This Operation Uses AI</h2>
          <vaadin-icon .svg="${ee.sparkles}"></vaadin-icon>
        `
    )}
      ${Ht(
      () => C`
          <p>AI is a third-party service that will receive some of your project code as context for the operation.</p>
          <label>
            <input
              id="ai-dialog-checkbox"
              type="checkbox"
              @change=${(t) => {
        this.rememberChoice = t.target.checked;
      }} />Don’t ask again
          </label>
        `
    )}
      ${Ft(
      () => C`
          <button @click=${() => this.sendEvent("cancel")}>Cancel</button>
          <button class="primary" @click=${() => this.sendEvent("ok")}>OK</button>
        `
    )}></vaadin-dialog>`;
  }
  sendEvent(t) {
    this.dispatchEvent(
      new CustomEvent("ai-usage-response", {
        detail: { response: t, rememberChoice: this.rememberChoice }
      })
    );
  }
  addESCListener() {
    document.addEventListener("keydown", this.handleESC, { capture: !0 });
  }
  removeESCListener() {
    document.removeEventListener("keydown", this.handleESC, { capture: !0 });
  }
};
Ni([
  Te()
], Kr.prototype, "opened", 2);
Kr = Ni([
  Le("copilot-ai-usage-confirmation-dialog")
], Kr);
var Fa = Object.defineProperty, Va = Object.getOwnPropertyDescriptor, Fe = (t, e, r, n) => {
  for (var i = n > 1 ? void 0 : n ? Va(e, r) : e, a = t.length - 1, o; a >= 0; a--)
    (o = t[a]) && (i = (n ? o(e, r, i) : o(i)) || i);
  return n && i && Fa(e, r, i), i;
};
const xn = {
  info: "UI state info",
  stacktrace: "Exception details",
  versions: "Vaadin, Java, OS, etc.."
};
let _e = class extends Mt {
  constructor() {
    super(...arguments), this.exceptionReport = void 0, this.dialogOpened = !1, this.visibleItemIndex = 0, this.versions = void 0, this.selectedItems = [], this.eventListener = (t) => {
      this.exceptionReport = t.detail, this.selectedItems = this.exceptionReport.items.map((e, r) => r), this.visibleItemIndex = 0, this.searchInputValue = void 0, this.dialogOpened = this.exceptionReport !== void 0;
    };
  }
  connectedCallback() {
    super.connectedCallback(), X.on("submit-exception-report-clicked", this.eventListener);
  }
  createRenderRoot() {
    return this;
  }
  disconnectedCallback() {
    super.disconnectedCallback(), X.off("submit-exception-report-clicked", this.eventListener);
  }
  close() {
    this.dialogOpened = !1;
  }
  clear() {
    this.exceptionReport = void 0;
  }
  render() {
    let t = "";
    return this.exceptionReport && this.exceptionReport.items.length > 0 && (t = this.exceptionReport.items[this.visibleItemIndex].content), C` <vaadin-dialog
      id="report-exception-dialog"
      no-close-on-outside-click
      draggable
      ?opened=${this.dialogOpened}
      @closed="${() => {
      this.clear();
    }}"
      @opened-changed="${(e) => {
      e.detail.value || this.close();
    }}"
      ${kt(
      () => C`
          <div
            class="draggable"
            style="display: flex; justify-content: space-between; align-items: center; width: 100%">
            <h2>Send report</h2>
            <vaadin-button theme="tertiary" @click="${this.close}">
              <vaadin-icon icon="lumo:cross"></vaadin-icon>
            </vaadin-button>
          </div>
        `
    )}
      ${Ht(
      () => C`
          <div class="description-container">
            <vaadin-text-area
              @input=${(e) => {
        this.searchInputValue = e.target.value;
      }}
              label="Description of the Bug"
              placeholder="A short, concise description of the bug and why you consider it a bug."></vaadin-text-area>
          </div>
          <div class="list-preview-container">
            <div class="left-menu">
              <div class="section-title">Include in Report</div>
              <vaadin-list-box
                single
                selected="${this.visibleItemIndex}"
                @selected-changed="${(e) => {
        this.visibleItemIndex = e.detail.value;
      }}">
                ${this.exceptionReport?.items.map(
        (e, r) => C` <vaadin-item>
                      <input
                        type="checkbox"
                        .checked="${this.selectedItems.indexOf(r) !== -1}"
                        @change="${(n) => {
          const i = n.target, a = [...this.selectedItems];
          if (i.checked)
            a.push(r), this.selectedItems = [...this.selectedItems];
          else {
            const o = this.selectedItems.indexOf(r);
            a.splice(o, 1);
          }
          this.selectedItems = a;
        }}" />
                      <div class="item-content">
                        <span class="item-name"> ${e.name} </span>
                        <span class="item-description">${this.renderItemDescription(e)}</span>
                      </div>
                    </vaadin-item>`
      )}
              </vaadin-list-box>
            </div>
            <div class="right-menu">
              <div class="section-title">Preview: ${this.exceptionReport?.items[this.visibleItemIndex].name}</div>
              <code class="codeblock">${t}</code>
            </div>
          </div>
        `,
      [this.exceptionReport, this.visibleItemIndex, this.selectedItems]
    )}
      ${Ft(
      () => C`
          <button
            id="cancel"
            @click=${() => {
        this.close();
      }}>
            Cancel
          </button>

          <button
            id="submit"
            class="primary"
            @click=${() => {
        this.submitErrorToGithub(), this.close();
      }}>
            Submit in GitHub
            <vaadin-tooltip
              for="submit"
              text="${this.bodyLengthExceeds() ? "The error report will be copied to clipboard and blank new issue page will be opened" : "New issue page will be opened with data loaded"}"
              position="top-start"></vaadin-tooltip>
          </button>
        `,
      [this.exceptionReport, this.selectedItems, this.searchInputValue]
    )}></vaadin-dialog>`;
  }
  renderItemDescription(t) {
    return Object.keys(xn).indexOf(t.name.toLowerCase()) !== -1 ? xn[t.name.toLowerCase()] : null;
  }
  bodyLengthExceeds() {
    const t = this.getIssueBodyNotEncoded();
    return t !== void 0 && encodeURIComponent(t).length > 7500;
  }
  getIssueBodyNotEncoded() {
    if (!this.exceptionReport)
      return;
    const t = this.exceptionReport.items.filter((e, r) => this.selectedItems.indexOf(r) !== -1).map((e) => {
      let r = "```";
      return e.name.includes(".java") && (r = `${r}java`), `## ${e.name} 
 
 ${r}
${e.content}
\`\`\``;
    });
    return this.searchInputValue ? `## Description of the bug 
 ${this.searchInputValue} 
 ${t.join(`
`)}` : `## Description of the bug 
 Please enter bug description here 
 ${t.join(`
`)}`;
  }
  submitErrorToGithub() {
    const t = this.exceptionReport;
    if (!t)
      return;
    const e = encodeURIComponent(t.title ?? "Bug report "), r = this.getIssueBodyNotEncoded();
    if (!r)
      return;
    let n = encodeURIComponent(r);
    n.length >= 7500 && (_i(r), n = encodeURIComponent("Please paste report here. It was automatically added to your clipboard."));
    const i = `https://github.com/vaadin/copilot/issues/new?title=${e}&body=${n}`;
    window.open(i, "_blank");
  }
};
Fe([
  ve()
], _e.prototype, "exceptionReport", 2);
Fe([
  ve()
], _e.prototype, "dialogOpened", 2);
Fe([
  ve()
], _e.prototype, "visibleItemIndex", 2);
Fe([
  ve()
], _e.prototype, "versions", 2);
Fe([
  ve()
], _e.prototype, "selectedItems", 2);
Fe([
  ve()
], _e.prototype, "searchInputValue", 2);
_e = Fe([
  Le("copilot-report-exception-dialog")
], _e);
let ht;
X.on("copilot-project-compilation-error", (t) => {
  if (t.detail.error) {
    let e;
    if (t.detail.files && t.detail.files.length > 0) {
      const r = O.idePluginState?.supportedActions?.includes("undo") ? C`
            <vaadin-button
              @click="${(n) => {
        n.preventDefault(), X.emit("undoRedo", { undo: !0, files: t.detail.files?.map((i) => i.path) });
      }}"
              theme="primary">
              <vaadin-icon slot="prefix" .svg="${ee.undo}"></vaadin-icon>
              Undo Last Change
            </vaadin-button>
          ` : Oe;
      e = Nt(C`
        <span> Following files have compilation errors: </span>
        <ul class="list-none mb-0 mt-2 p-0">
          ${t.detail.files.map(
        (n) => C` <li>
                <vaadin-button
                  @click="${() => {
          X.emit("show-in-ide", { javaSource: { absoluteFilePath: n.path } });
        }}"
                  theme="tertiary">
                  <vaadin-icon slot="prefix" .svg="${ee.code}"></vaadin-icon>
                  ${n.name}
                </vaadin-button>
              </li>`
      )}
        </ul>
        <hr class="border-b border-e-0 border-s-0 border-t-0 mb-3 mt-2 mx-0 w-full" />
        ${r}
      `);
    } else
      e = "Project contains one or more compilation errors.";
    ht = Pe({
      message: "Compilation error",
      details: e,
      type: se.WARNING,
      delay: 3e4
    });
  } else
    ht && Pi(ht), ht = void 0;
});
var qa = Object.defineProperty, Ba = Object.getOwnPropertyDescriptor, Hi = (t, e, r, n) => {
  for (var i = n > 1 ? void 0 : n ? Ba(e, r) : e, a = t.length - 1, o; a >= 0; a--)
    (o = t[a]) && (i = (n ? o(e, r, i) : o(i)) || i);
  return n && i && qa(e, r, i), i;
};
let Qr = class extends Mt {
  constructor() {
    super(...arguments), this.text = () => (this.parentElement.textContent ?? "").trim();
  }
  createRenderRoot() {
    return this;
  }
  render() {
    return C`<vaadin-button
      aria-label="Copy to clipboard"
      @click=${(t) => {
      t.stopPropagation(), t.preventDefault();
      const e = this.text();
      _i(e);
    }}
      theme="icon tertiary">
      <vaadin-icon .svg="${ee.fileCopy}"></vaadin-icon>
      <vaadin-tooltip slot="tooltip" text="Copy to clipboard"></vaadin-tooltip>
    </vaadin-button> `;
  }
};
Hi([
  Te({ type: Function })
], Qr.prototype, "text", 2);
Qr = Hi([
  Le("copilot-copy")
], Qr);
var ja = {
  202: "Accepted",
  502: "Bad Gateway",
  400: "Bad Request",
  409: "Conflict",
  100: "Continue",
  201: "Created",
  417: "Expectation Failed",
  424: "Failed Dependency",
  403: "Forbidden",
  504: "Gateway Timeout",
  410: "Gone",
  505: "HTTP Version Not Supported",
  418: "I'm a teapot",
  419: "Insufficient Space on Resource",
  507: "Insufficient Storage",
  500: "Internal Server Error",
  411: "Length Required",
  423: "Locked",
  420: "Method Failure",
  405: "Method Not Allowed",
  301: "Moved Permanently",
  302: "Moved Temporarily",
  207: "Multi-Status",
  300: "Multiple Choices",
  511: "Network Authentication Required",
  204: "No Content",
  203: "Non Authoritative Information",
  406: "Not Acceptable",
  404: "Not Found",
  501: "Not Implemented",
  304: "Not Modified",
  200: "OK",
  206: "Partial Content",
  402: "Payment Required",
  308: "Permanent Redirect",
  412: "Precondition Failed",
  428: "Precondition Required",
  102: "Processing",
  103: "Early Hints",
  426: "Upgrade Required",
  407: "Proxy Authentication Required",
  431: "Request Header Fields Too Large",
  408: "Request Timeout",
  413: "Request Entity Too Large",
  414: "Request-URI Too Long",
  416: "Requested Range Not Satisfiable",
  205: "Reset Content",
  303: "See Other",
  503: "Service Unavailable",
  101: "Switching Protocols",
  307: "Temporary Redirect",
  429: "Too Many Requests",
  401: "Unauthorized",
  451: "Unavailable For Legal Reasons",
  422: "Unprocessable Entity",
  415: "Unsupported Media Type",
  305: "Use Proxy",
  421: "Misdirected Request"
};
function Za(t) {
  var e = ja[t.toString()];
  if (!e)
    throw new Error("Status code does not exist: " + t);
  return e;
}
function ki(t) {
  return `endpoint-request-${t.id}`;
}
X.on("endpoint-request", (t) => {
  const e = t.detail, r = ki(e);
  delete e.id;
  const n = Object.values(e.params), i = n.map(Tt).join(", ");
  X.emit("log", {
    id: r,
    type: se.INFORMATION,
    message: `Called endpoint ${e.endpoint}.${e.method}(${i})`,
    expandedMessage: Nt(
      C`Called endpoint ${e.endpoint}.${e.method} with parameters
        <code class="codeblock"><copilot-copy></copilot-copy>${Tt(n)}</code>`
    ),
    details: "Response: <pending>"
  });
});
X.on("endpoint-response", (t) => {
  let e;
  try {
    e = JSON.parse(t.detail.text);
  } catch {
    e = t.detail.text;
  }
  const r = {}, n = t.detail.status ?? 200;
  n === 200 ? (r.details = `Response: ${Tt(e)}`, r.expandedDetails = Nt(
    C`Response: <code class="codeblock"><copilot-copy></copilot-copy>${Tt(e)}</code>`
  )) : (r.details = `Error: ${n} ${Za(n)}`, r.type = se.ERROR), X.emit("update-log", {
    id: ki(t.detail),
    ...r
  });
});
function Tt(t) {
  return typeof t == "string" ? `${t}` : JSON.stringify(t, void 0, 2);
}
var Ga = Object.defineProperty, Ua = Object.getOwnPropertyDescriptor, Me = (t, e, r, n) => {
  for (var i = n > 1 ? void 0 : n ? Ua(e, r) : e, a = t.length - 1, o; a >= 0; a--)
    (o = t[a]) && (i = (n ? o(e, r, i) : o(i)) || i);
  return n && i && Ga(e, r, i), i;
};
class Xa extends CustomEvent {
  constructor(e) {
    super("show-in-ide-clicked", {
      detail: e,
      bubbles: !0,
      composed: !0
    });
  }
}
let ge = class extends Mt {
  constructor() {
    super(...arguments), this.iconHidden = !1, this.linkHidden = !1, this.tooltipText = void 0, this.linkText = void 0, this.source = void 0, this.javaSource = void 0, this.node = void 0;
  }
  static get styles() {
    return [In(ma), In(ga)];
  }
  render() {
    if (this.iconHidden) {
      if (!this.linkHidden)
        return this.renderContent(this.renderAnchor());
    } else return this.linkHidden ? this.renderContent(this.renderIcon()) : this.renderContent([this.renderIcon(), this.renderAnchor()]);
    return Oe;
  }
  renderContent(t) {
    return C` <div class="contents">${t}</div> `;
  }
  renderIcon() {
    const t = this.tooltipText ?? `Open ${this.getFileName()} in IDE`;
    return C`
      <vaadin-button
        aria-label="${t}"
        id="show-in-ide"
        theme="icon tertiary"
        @click=${(e) => {
      e.stopPropagation(), e.preventDefault(), this._showInIde();
    }}>
        <vaadin-icon .svg="${ee.code}"></vaadin-icon>
        <vaadin-tooltip slot="tooltip" text="${t}"></vaadin-tooltip>
      </vaadin-button>
    `;
  }
  renderAnchor() {
    return C`
      <a
        class="text-blue-11"
        href="#"
        id="link"
        @click=${(t) => {
      t.preventDefault(), this._showInIde();
    }}
        >${this.linkText ?? this.getFileName() ?? ""}</a
      >
      ${this.renderTooltip("link")}
    `;
  }
  dispatchClickedEvent() {
    this.dispatchEvent(
      new Xa({
        source: this.source,
        javaSource: this.javaSource,
        node: this.node
      })
    );
  }
  renderTooltip(t) {
    const e = this.tooltipText ?? `Open ${this.getFileName()} in IDE`;
    return C`<vaadin-tooltip for="${t}" text="${e}" position="top-start"></vaadin-tooltip>`;
  }
  getFileName() {
    if (this.tooltipText)
      return this.tooltipText;
    if (this.source && this.source.fileName)
      return va(this.source.fileName);
    if (this.javaSource)
      return this.javaSource.className;
  }
  _showInIde() {
    X.emit("show-in-ide", {
      source: this.source,
      javaSource: this.javaSource,
      node: this.node
    }), this.dispatchClickedEvent();
  }
};
ge.TAG = "copilot-go-to-source";
Me([
  Te({ type: Boolean })
], ge.prototype, "iconHidden", 2);
Me([
  Te({ type: Boolean })
], ge.prototype, "linkHidden", 2);
Me([
  Te()
], ge.prototype, "tooltipText", 2);
Me([
  Te()
], ge.prototype, "linkText", 2);
Me([
  Te()
], ge.prototype, "source", 2);
Me([
  Te()
], ge.prototype, "javaSource", 2);
Me([
  Te()
], ge.prototype, "node", 2);
ge = Me([
  Le(ge.TAG)
], ge);
/**!
 * Sortable 1.15.6
 * @author	RubaXa   <trash@rubaxa.org>
 * @author	owenm    <owen23355@gmail.com>
 * @license MIT
 */
function Cn(t, e) {
  var r = Object.keys(t);
  if (Object.getOwnPropertySymbols) {
    var n = Object.getOwnPropertySymbols(t);
    e && (n = n.filter(function(i) {
      return Object.getOwnPropertyDescriptor(t, i).enumerable;
    })), r.push.apply(r, n);
  }
  return r;
}
function we(t) {
  for (var e = 1; e < arguments.length; e++) {
    var r = arguments[e] != null ? arguments[e] : {};
    e % 2 ? Cn(Object(r), !0).forEach(function(n) {
      Wa(t, n, r[n]);
    }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(t, Object.getOwnPropertyDescriptors(r)) : Cn(Object(r)).forEach(function(n) {
      Object.defineProperty(t, n, Object.getOwnPropertyDescriptor(r, n));
    });
  }
  return t;
}
function bt(t) {
  "@babel/helpers - typeof";
  return typeof Symbol == "function" && typeof Symbol.iterator == "symbol" ? bt = function(e) {
    return typeof e;
  } : bt = function(e) {
    return e && typeof Symbol == "function" && e.constructor === Symbol && e !== Symbol.prototype ? "symbol" : typeof e;
  }, bt(t);
}
function Wa(t, e, r) {
  return e in t ? Object.defineProperty(t, e, {
    value: r,
    enumerable: !0,
    configurable: !0,
    writable: !0
  }) : t[e] = r, t;
}
function $e() {
  return $e = Object.assign || function(t) {
    for (var e = 1; e < arguments.length; e++) {
      var r = arguments[e];
      for (var n in r)
        Object.prototype.hasOwnProperty.call(r, n) && (t[n] = r[n]);
    }
    return t;
  }, $e.apply(this, arguments);
}
function Ya(t, e) {
  if (t == null) return {};
  var r = {}, n = Object.keys(t), i, a;
  for (a = 0; a < n.length; a++)
    i = n[a], !(e.indexOf(i) >= 0) && (r[i] = t[i]);
  return r;
}
function za(t, e) {
  if (t == null) return {};
  var r = Ya(t, e), n, i;
  if (Object.getOwnPropertySymbols) {
    var a = Object.getOwnPropertySymbols(t);
    for (i = 0; i < a.length; i++)
      n = a[i], !(e.indexOf(n) >= 0) && Object.prototype.propertyIsEnumerable.call(t, n) && (r[n] = t[n]);
  }
  return r;
}
var Ja = "1.15.6";
function Se(t) {
  if (typeof window < "u" && window.navigator)
    return !!/* @__PURE__ */ navigator.userAgent.match(t);
}
var xe = Se(/(?:Trident.*rv[ :]?11\.|msie|iemobile|Windows Phone)/i), lt = Se(/Edge/i), Dn = Se(/firefox/i), tt = Se(/safari/i) && !Se(/chrome/i) && !Se(/android/i), fn = Se(/iP(ad|od|hone)/i), Fi = Se(/chrome/i) && Se(/android/i), Vi = {
  capture: !1,
  passive: !1
};
function H(t, e, r) {
  t.addEventListener(e, r, !xe && Vi);
}
function L(t, e, r) {
  t.removeEventListener(e, r, !xe && Vi);
}
function xt(t, e) {
  if (e) {
    if (e[0] === ">" && (e = e.substring(1)), t)
      try {
        if (t.matches)
          return t.matches(e);
        if (t.msMatchesSelector)
          return t.msMatchesSelector(e);
        if (t.webkitMatchesSelector)
          return t.webkitMatchesSelector(e);
      } catch {
        return !1;
      }
    return !1;
  }
}
function qi(t) {
  return t.host && t !== document && t.host.nodeType ? t.host : t.parentNode;
}
function me(t, e, r, n) {
  if (t) {
    r = r || document;
    do {
      if (e != null && (e[0] === ">" ? t.parentNode === r && xt(t, e) : xt(t, e)) || n && t === r)
        return t;
      if (t === r) break;
    } while (t = qi(t));
  }
  return null;
}
var On = /\s+/g;
function de(t, e, r) {
  if (t && e)
    if (t.classList)
      t.classList[r ? "add" : "remove"](e);
    else {
      var n = (" " + t.className + " ").replace(On, " ").replace(" " + e + " ", " ");
      t.className = (n + (r ? " " + e : "")).replace(On, " ");
    }
}
function S(t, e, r) {
  var n = t && t.style;
  if (n) {
    if (r === void 0)
      return document.defaultView && document.defaultView.getComputedStyle ? r = document.defaultView.getComputedStyle(t, "") : t.currentStyle && (r = t.currentStyle), e === void 0 ? r : r[e];
    !(e in n) && e.indexOf("webkit") === -1 && (e = "-webkit-" + e), n[e] = r + (typeof r == "string" ? "" : "px");
  }
}
function Xe(t, e) {
  var r = "";
  if (typeof t == "string")
    r = t;
  else
    do {
      var n = S(t, "transform");
      n && n !== "none" && (r = n + " " + r);
    } while (!e && (t = t.parentNode));
  var i = window.DOMMatrix || window.WebKitCSSMatrix || window.CSSMatrix || window.MSCSSMatrix;
  return i && new i(r);
}
function Bi(t, e, r) {
  if (t) {
    var n = t.getElementsByTagName(e), i = 0, a = n.length;
    if (r)
      for (; i < a; i++)
        r(n[i], i);
    return n;
  }
  return [];
}
function ye() {
  var t = document.scrollingElement;
  return t || document.documentElement;
}
function z(t, e, r, n, i) {
  if (!(!t.getBoundingClientRect && t !== window)) {
    var a, o, d, c, s, l, u;
    if (t !== window && t.parentNode && t !== ye() ? (a = t.getBoundingClientRect(), o = a.top, d = a.left, c = a.bottom, s = a.right, l = a.height, u = a.width) : (o = 0, d = 0, c = window.innerHeight, s = window.innerWidth, l = window.innerHeight, u = window.innerWidth), (e || r) && t !== window && (i = i || t.parentNode, !xe))
      do
        if (i && i.getBoundingClientRect && (S(i, "transform") !== "none" || r && S(i, "position") !== "static")) {
          var h = i.getBoundingClientRect();
          o -= h.top + parseInt(S(i, "border-top-width")), d -= h.left + parseInt(S(i, "border-left-width")), c = o + a.height, s = d + a.width;
          break;
        }
      while (i = i.parentNode);
    if (n && t !== window) {
      var p = Xe(i || t), b = p && p.a, f = p && p.d;
      p && (o /= f, d /= b, u /= b, l /= f, c = o + l, s = d + u);
    }
    return {
      top: o,
      left: d,
      bottom: c,
      right: s,
      width: u,
      height: l
    };
  }
}
function An(t, e, r) {
  for (var n = Ae(t, !0), i = z(t)[e]; n; ) {
    var a = z(n)[r], o = void 0;
    if (o = i >= a, !o) return n;
    if (n === ye()) break;
    n = Ae(n, !1);
  }
  return !1;
}
function We(t, e, r, n) {
  for (var i = 0, a = 0, o = t.children; a < o.length; ) {
    if (o[a].style.display !== "none" && o[a] !== $.ghost && (n || o[a] !== $.dragged) && me(o[a], r.draggable, t, !1)) {
      if (i === e)
        return o[a];
      i++;
    }
    a++;
  }
  return null;
}
function pn(t, e) {
  for (var r = t.lastElementChild; r && (r === $.ghost || S(r, "display") === "none" || e && !xt(r, e)); )
    r = r.previousElementSibling;
  return r || null;
}
function fe(t, e) {
  var r = 0;
  if (!t || !t.parentNode)
    return -1;
  for (; t = t.previousElementSibling; )
    t.nodeName.toUpperCase() !== "TEMPLATE" && t !== $.clone && (!e || xt(t, e)) && r++;
  return r;
}
function Pn(t) {
  var e = 0, r = 0, n = ye();
  if (t)
    do {
      var i = Xe(t), a = i.a, o = i.d;
      e += t.scrollLeft * a, r += t.scrollTop * o;
    } while (t !== n && (t = t.parentNode));
  return [e, r];
}
function Ka(t, e) {
  for (var r in t)
    if (t.hasOwnProperty(r)) {
      for (var n in e)
        if (e.hasOwnProperty(n) && e[n] === t[r][n]) return Number(r);
    }
  return -1;
}
function Ae(t, e) {
  if (!t || !t.getBoundingClientRect) return ye();
  var r = t, n = !1;
  do
    if (r.clientWidth < r.scrollWidth || r.clientHeight < r.scrollHeight) {
      var i = S(r);
      if (r.clientWidth < r.scrollWidth && (i.overflowX == "auto" || i.overflowX == "scroll") || r.clientHeight < r.scrollHeight && (i.overflowY == "auto" || i.overflowY == "scroll")) {
        if (!r.getBoundingClientRect || r === document.body) return ye();
        if (n || e) return r;
        n = !0;
      }
    }
  while (r = r.parentNode);
  return ye();
}
function Qa(t, e) {
  if (t && e)
    for (var r in e)
      e.hasOwnProperty(r) && (t[r] = e[r]);
  return t;
}
function zt(t, e) {
  return Math.round(t.top) === Math.round(e.top) && Math.round(t.left) === Math.round(e.left) && Math.round(t.height) === Math.round(e.height) && Math.round(t.width) === Math.round(e.width);
}
var rt;
function ji(t, e) {
  return function() {
    if (!rt) {
      var r = arguments, n = this;
      r.length === 1 ? t.call(n, r[0]) : t.apply(n, r), rt = setTimeout(function() {
        rt = void 0;
      }, e);
    }
  };
}
function eo() {
  clearTimeout(rt), rt = void 0;
}
function Zi(t, e, r) {
  t.scrollLeft += e, t.scrollTop += r;
}
function Gi(t) {
  var e = window.Polymer, r = window.jQuery || window.Zepto;
  return e && e.dom ? e.dom(t).cloneNode(!0) : r ? r(t).clone(!0)[0] : t.cloneNode(!0);
}
function Ui(t, e, r) {
  var n = {};
  return Array.from(t.children).forEach(function(i) {
    var a, o, d, c;
    if (!(!me(i, e.draggable, t, !1) || i.animated || i === r)) {
      var s = z(i);
      n.left = Math.min((a = n.left) !== null && a !== void 0 ? a : 1 / 0, s.left), n.top = Math.min((o = n.top) !== null && o !== void 0 ? o : 1 / 0, s.top), n.right = Math.max((d = n.right) !== null && d !== void 0 ? d : -1 / 0, s.right), n.bottom = Math.max((c = n.bottom) !== null && c !== void 0 ? c : -1 / 0, s.bottom);
    }
  }), n.width = n.right - n.left, n.height = n.bottom - n.top, n.x = n.left, n.y = n.top, n;
}
var le = "Sortable" + (/* @__PURE__ */ new Date()).getTime();
function to() {
  var t = [], e;
  return {
    captureAnimationState: function() {
      if (t = [], !!this.options.animation) {
        var n = [].slice.call(this.el.children);
        n.forEach(function(i) {
          if (!(S(i, "display") === "none" || i === $.ghost)) {
            t.push({
              target: i,
              rect: z(i)
            });
            var a = we({}, t[t.length - 1].rect);
            if (i.thisAnimationDuration) {
              var o = Xe(i, !0);
              o && (a.top -= o.f, a.left -= o.e);
            }
            i.fromRect = a;
          }
        });
      }
    },
    addAnimationState: function(n) {
      t.push(n);
    },
    removeAnimationState: function(n) {
      t.splice(Ka(t, {
        target: n
      }), 1);
    },
    animateAll: function(n) {
      var i = this;
      if (!this.options.animation) {
        clearTimeout(e), typeof n == "function" && n();
        return;
      }
      var a = !1, o = 0;
      t.forEach(function(d) {
        var c = 0, s = d.target, l = s.fromRect, u = z(s), h = s.prevFromRect, p = s.prevToRect, b = d.rect, f = Xe(s, !0);
        f && (u.top -= f.f, u.left -= f.e), s.toRect = u, s.thisAnimationDuration && zt(h, u) && !zt(l, u) && // Make sure animatingRect is on line between toRect & fromRect
        (b.top - u.top) / (b.left - u.left) === (l.top - u.top) / (l.left - u.left) && (c = no(b, h, p, i.options)), zt(u, l) || (s.prevFromRect = l, s.prevToRect = u, c || (c = i.options.animation), i.animate(s, b, u, c)), c && (a = !0, o = Math.max(o, c), clearTimeout(s.animationResetTimer), s.animationResetTimer = setTimeout(function() {
          s.animationTime = 0, s.prevFromRect = null, s.fromRect = null, s.prevToRect = null, s.thisAnimationDuration = null;
        }, c), s.thisAnimationDuration = c);
      }), clearTimeout(e), a ? e = setTimeout(function() {
        typeof n == "function" && n();
      }, o) : typeof n == "function" && n(), t = [];
    },
    animate: function(n, i, a, o) {
      if (o) {
        S(n, "transition", ""), S(n, "transform", "");
        var d = Xe(this.el), c = d && d.a, s = d && d.d, l = (i.left - a.left) / (c || 1), u = (i.top - a.top) / (s || 1);
        n.animatingX = !!l, n.animatingY = !!u, S(n, "transform", "translate3d(" + l + "px," + u + "px,0)"), this.forRepaintDummy = ro(n), S(n, "transition", "transform " + o + "ms" + (this.options.easing ? " " + this.options.easing : "")), S(n, "transform", "translate3d(0,0,0)"), typeof n.animated == "number" && clearTimeout(n.animated), n.animated = setTimeout(function() {
          S(n, "transition", ""), S(n, "transform", ""), n.animated = !1, n.animatingX = !1, n.animatingY = !1;
        }, o);
      }
    }
  };
}
function ro(t) {
  return t.offsetWidth;
}
function no(t, e, r, n) {
  return Math.sqrt(Math.pow(e.top - t.top, 2) + Math.pow(e.left - t.left, 2)) / Math.sqrt(Math.pow(e.top - r.top, 2) + Math.pow(e.left - r.left, 2)) * n.animation;
}
var qe = [], Jt = {
  initializeByDefault: !0
}, ct = {
  mount: function(e) {
    for (var r in Jt)
      Jt.hasOwnProperty(r) && !(r in e) && (e[r] = Jt[r]);
    qe.forEach(function(n) {
      if (n.pluginName === e.pluginName)
        throw "Sortable: Cannot mount plugin ".concat(e.pluginName, " more than once");
    }), qe.push(e);
  },
  pluginEvent: function(e, r, n) {
    var i = this;
    this.eventCanceled = !1, n.cancel = function() {
      i.eventCanceled = !0;
    };
    var a = e + "Global";
    qe.forEach(function(o) {
      r[o.pluginName] && (r[o.pluginName][a] && r[o.pluginName][a](we({
        sortable: r
      }, n)), r.options[o.pluginName] && r[o.pluginName][e] && r[o.pluginName][e](we({
        sortable: r
      }, n)));
    });
  },
  initializePlugins: function(e, r, n, i) {
    qe.forEach(function(d) {
      var c = d.pluginName;
      if (!(!e.options[c] && !d.initializeByDefault)) {
        var s = new d(e, r, e.options);
        s.sortable = e, s.options = e.options, e[c] = s, $e(n, s.defaults);
      }
    });
    for (var a in e.options)
      if (e.options.hasOwnProperty(a)) {
        var o = this.modifyOption(e, a, e.options[a]);
        typeof o < "u" && (e.options[a] = o);
      }
  },
  getEventProperties: function(e, r) {
    var n = {};
    return qe.forEach(function(i) {
      typeof i.eventProperties == "function" && $e(n, i.eventProperties.call(r[i.pluginName], e));
    }), n;
  },
  modifyOption: function(e, r, n) {
    var i;
    return qe.forEach(function(a) {
      e[a.pluginName] && a.optionListeners && typeof a.optionListeners[r] == "function" && (i = a.optionListeners[r].call(e[a.pluginName], n));
    }), i;
  }
};
function io(t) {
  var e = t.sortable, r = t.rootEl, n = t.name, i = t.targetEl, a = t.cloneEl, o = t.toEl, d = t.fromEl, c = t.oldIndex, s = t.newIndex, l = t.oldDraggableIndex, u = t.newDraggableIndex, h = t.originalEvent, p = t.putSortable, b = t.extraEventProperties;
  if (e = e || r && r[le], !!e) {
    var f, I = e.options, _ = "on" + n.charAt(0).toUpperCase() + n.substr(1);
    window.CustomEvent && !xe && !lt ? f = new CustomEvent(n, {
      bubbles: !0,
      cancelable: !0
    }) : (f = document.createEvent("Event"), f.initEvent(n, !0, !0)), f.to = o || r, f.from = d || r, f.item = i || r, f.clone = a, f.oldIndex = c, f.newIndex = s, f.oldDraggableIndex = l, f.newDraggableIndex = u, f.originalEvent = h, f.pullMode = p ? p.lastPutMode : void 0;
    var k = we(we({}, b), ct.getEventProperties(n, e));
    for (var A in k)
      f[A] = k[A];
    r && r.dispatchEvent(f), I[_] && I[_].call(e, f);
  }
}
var ao = ["evt"], oe = function(e, r) {
  var n = arguments.length > 2 && arguments[2] !== void 0 ? arguments[2] : {}, i = n.evt, a = za(n, ao);
  ct.pluginEvent.bind($)(e, r, we({
    dragEl: g,
    parentEl: U,
    ghostEl: x,
    rootEl: j,
    nextEl: ke,
    lastDownEl: Et,
    cloneEl: Z,
    cloneHidden: De,
    dragStarted: Je,
    putSortable: Q,
    activeSortable: $.active,
    originalEvent: i,
    oldIndex: Ge,
    oldDraggableIndex: nt,
    newIndex: ue,
    newDraggableIndex: Ce,
    hideGhostForTarget: zi,
    unhideGhostForTarget: Ji,
    cloneNowHidden: function() {
      De = !0;
    },
    cloneNowShown: function() {
      De = !1;
    },
    dispatchSortableEvent: function(d) {
      ne({
        sortable: r,
        name: d,
        originalEvent: i
      });
    }
  }, a));
};
function ne(t) {
  io(we({
    putSortable: Q,
    cloneEl: Z,
    targetEl: g,
    rootEl: j,
    oldIndex: Ge,
    oldDraggableIndex: nt,
    newIndex: ue,
    newDraggableIndex: Ce
  }, t));
}
var g, U, x, j, ke, Et, Z, De, Ge, ue, nt, Ce, ft, Q, Ze = !1, Ct = !1, Dt = [], Ne, pe, Kt, Qt, _n, Ln, Je, Be, it, at = !1, pt = !1, yt, te, er = [], en = !1, Ot = [], Vt = typeof document < "u", mt = fn, Mn = lt || xe ? "cssFloat" : "float", oo = Vt && !Fi && !fn && "draggable" in document.createElement("div"), Xi = function() {
  if (Vt) {
    if (xe)
      return !1;
    var t = document.createElement("x");
    return t.style.cssText = "pointer-events:auto", t.style.pointerEvents === "auto";
  }
}(), Wi = function(e, r) {
  var n = S(e), i = parseInt(n.width) - parseInt(n.paddingLeft) - parseInt(n.paddingRight) - parseInt(n.borderLeftWidth) - parseInt(n.borderRightWidth), a = We(e, 0, r), o = We(e, 1, r), d = a && S(a), c = o && S(o), s = d && parseInt(d.marginLeft) + parseInt(d.marginRight) + z(a).width, l = c && parseInt(c.marginLeft) + parseInt(c.marginRight) + z(o).width;
  if (n.display === "flex")
    return n.flexDirection === "column" || n.flexDirection === "column-reverse" ? "vertical" : "horizontal";
  if (n.display === "grid")
    return n.gridTemplateColumns.split(" ").length <= 1 ? "vertical" : "horizontal";
  if (a && d.float && d.float !== "none") {
    var u = d.float === "left" ? "left" : "right";
    return o && (c.clear === "both" || c.clear === u) ? "vertical" : "horizontal";
  }
  return a && (d.display === "block" || d.display === "flex" || d.display === "table" || d.display === "grid" || s >= i && n[Mn] === "none" || o && n[Mn] === "none" && s + l > i) ? "vertical" : "horizontal";
}, so = function(e, r, n) {
  var i = n ? e.left : e.top, a = n ? e.right : e.bottom, o = n ? e.width : e.height, d = n ? r.left : r.top, c = n ? r.right : r.bottom, s = n ? r.width : r.height;
  return i === d || a === c || i + o / 2 === d + s / 2;
}, lo = function(e, r) {
  var n;
  return Dt.some(function(i) {
    var a = i[le].options.emptyInsertThreshold;
    if (!(!a || pn(i))) {
      var o = z(i), d = e >= o.left - a && e <= o.right + a, c = r >= o.top - a && r <= o.bottom + a;
      if (d && c)
        return n = i;
    }
  }), n;
}, Yi = function(e) {
  function r(a, o) {
    return function(d, c, s, l) {
      var u = d.options.group.name && c.options.group.name && d.options.group.name === c.options.group.name;
      if (a == null && (o || u))
        return !0;
      if (a == null || a === !1)
        return !1;
      if (o && a === "clone")
        return a;
      if (typeof a == "function")
        return r(a(d, c, s, l), o)(d, c, s, l);
      var h = (o ? d : c).options.group.name;
      return a === !0 || typeof a == "string" && a === h || a.join && a.indexOf(h) > -1;
    };
  }
  var n = {}, i = e.group;
  (!i || bt(i) != "object") && (i = {
    name: i
  }), n.name = i.name, n.checkPull = r(i.pull, !0), n.checkPut = r(i.put), n.revertClone = i.revertClone, e.group = n;
}, zi = function() {
  !Xi && x && S(x, "display", "none");
}, Ji = function() {
  !Xi && x && S(x, "display", "");
};
Vt && !Fi && document.addEventListener("click", function(t) {
  if (Ct)
    return t.preventDefault(), t.stopPropagation && t.stopPropagation(), t.stopImmediatePropagation && t.stopImmediatePropagation(), Ct = !1, !1;
}, !0);
var He = function(e) {
  if (g) {
    e = e.touches ? e.touches[0] : e;
    var r = lo(e.clientX, e.clientY);
    if (r) {
      var n = {};
      for (var i in e)
        e.hasOwnProperty(i) && (n[i] = e[i]);
      n.target = n.rootEl = r, n.preventDefault = void 0, n.stopPropagation = void 0, r[le]._onDragOver(n);
    }
  }
}, co = function(e) {
  g && g.parentNode[le]._isOutsideThisEl(e.target);
};
function $(t, e) {
  if (!(t && t.nodeType && t.nodeType === 1))
    throw "Sortable: `el` must be an HTMLElement, not ".concat({}.toString.call(t));
  this.el = t, this.options = e = $e({}, e), t[le] = this;
  var r = {
    group: null,
    sort: !0,
    disabled: !1,
    store: null,
    handle: null,
    draggable: /^[uo]l$/i.test(t.nodeName) ? ">li" : ">*",
    swapThreshold: 1,
    // percentage; 0 <= x <= 1
    invertSwap: !1,
    // invert always
    invertedSwapThreshold: null,
    // will be set to same as swapThreshold if default
    removeCloneOnHide: !0,
    direction: function() {
      return Wi(t, this.options);
    },
    ghostClass: "sortable-ghost",
    chosenClass: "sortable-chosen",
    dragClass: "sortable-drag",
    ignore: "a, img",
    filter: null,
    preventOnFilter: !0,
    animation: 0,
    easing: null,
    setData: function(o, d) {
      o.setData("Text", d.textContent);
    },
    dropBubble: !1,
    dragoverBubble: !1,
    dataIdAttr: "data-id",
    delay: 0,
    delayOnTouchOnly: !1,
    touchStartThreshold: (Number.parseInt ? Number : window).parseInt(window.devicePixelRatio, 10) || 1,
    forceFallback: !1,
    fallbackClass: "sortable-fallback",
    fallbackOnBody: !1,
    fallbackTolerance: 0,
    fallbackOffset: {
      x: 0,
      y: 0
    },
    // Disabled on Safari: #1571; Enabled on Safari IOS: #2244
    supportPointer: $.supportPointer !== !1 && "PointerEvent" in window && (!tt || fn),
    emptyInsertThreshold: 5
  };
  ct.initializePlugins(this, t, r);
  for (var n in r)
    !(n in e) && (e[n] = r[n]);
  Yi(e);
  for (var i in this)
    i.charAt(0) === "_" && typeof this[i] == "function" && (this[i] = this[i].bind(this));
  this.nativeDraggable = e.forceFallback ? !1 : oo, this.nativeDraggable && (this.options.touchStartThreshold = 1), e.supportPointer ? H(t, "pointerdown", this._onTapStart) : (H(t, "mousedown", this._onTapStart), H(t, "touchstart", this._onTapStart)), this.nativeDraggable && (H(t, "dragover", this), H(t, "dragenter", this)), Dt.push(this.el), e.store && e.store.get && this.sort(e.store.get(this) || []), $e(this, to());
}
$.prototype = /** @lends Sortable.prototype */
{
  constructor: $,
  _isOutsideThisEl: function(e) {
    !this.el.contains(e) && e !== this.el && (Be = null);
  },
  _getDirection: function(e, r) {
    return typeof this.options.direction == "function" ? this.options.direction.call(this, e, r, g) : this.options.direction;
  },
  _onTapStart: function(e) {
    if (e.cancelable) {
      var r = this, n = this.el, i = this.options, a = i.preventOnFilter, o = e.type, d = e.touches && e.touches[0] || e.pointerType && e.pointerType === "touch" && e, c = (d || e).target, s = e.target.shadowRoot && (e.path && e.path[0] || e.composedPath && e.composedPath()[0]) || c, l = i.filter;
      if (bo(n), !g && !(/mousedown|pointerdown/.test(o) && e.button !== 0 || i.disabled) && !s.isContentEditable && !(!this.nativeDraggable && tt && c && c.tagName.toUpperCase() === "SELECT") && (c = me(c, i.draggable, n, !1), !(c && c.animated) && Et !== c)) {
        if (Ge = fe(c), nt = fe(c, i.draggable), typeof l == "function") {
          if (l.call(this, e, c, this)) {
            ne({
              sortable: r,
              rootEl: s,
              name: "filter",
              targetEl: c,
              toEl: n,
              fromEl: n
            }), oe("filter", r, {
              evt: e
            }), a && e.preventDefault();
            return;
          }
        } else if (l && (l = l.split(",").some(function(u) {
          if (u = me(s, u.trim(), n, !1), u)
            return ne({
              sortable: r,
              rootEl: u,
              name: "filter",
              targetEl: c,
              fromEl: n,
              toEl: n
            }), oe("filter", r, {
              evt: e
            }), !0;
        }), l)) {
          a && e.preventDefault();
          return;
        }
        i.handle && !me(s, i.handle, n, !1) || this._prepareDragStart(e, d, c);
      }
    }
  },
  _prepareDragStart: function(e, r, n) {
    var i = this, a = i.el, o = i.options, d = a.ownerDocument, c;
    if (n && !g && n.parentNode === a) {
      var s = z(n);
      if (j = a, g = n, U = g.parentNode, ke = g.nextSibling, Et = n, ft = o.group, $.dragged = g, Ne = {
        target: g,
        clientX: (r || e).clientX,
        clientY: (r || e).clientY
      }, _n = Ne.clientX - s.left, Ln = Ne.clientY - s.top, this._lastX = (r || e).clientX, this._lastY = (r || e).clientY, g.style["will-change"] = "all", c = function() {
        if (oe("delayEnded", i, {
          evt: e
        }), $.eventCanceled) {
          i._onDrop();
          return;
        }
        i._disableDelayedDragEvents(), !Dn && i.nativeDraggable && (g.draggable = !0), i._triggerDragStart(e, r), ne({
          sortable: i,
          name: "choose",
          originalEvent: e
        }), de(g, o.chosenClass, !0);
      }, o.ignore.split(",").forEach(function(l) {
        Bi(g, l.trim(), tr);
      }), H(d, "dragover", He), H(d, "mousemove", He), H(d, "touchmove", He), o.supportPointer ? (H(d, "pointerup", i._onDrop), !this.nativeDraggable && H(d, "pointercancel", i._onDrop)) : (H(d, "mouseup", i._onDrop), H(d, "touchend", i._onDrop), H(d, "touchcancel", i._onDrop)), Dn && this.nativeDraggable && (this.options.touchStartThreshold = 4, g.draggable = !0), oe("delayStart", this, {
        evt: e
      }), o.delay && (!o.delayOnTouchOnly || r) && (!this.nativeDraggable || !(lt || xe))) {
        if ($.eventCanceled) {
          this._onDrop();
          return;
        }
        o.supportPointer ? (H(d, "pointerup", i._disableDelayedDrag), H(d, "pointercancel", i._disableDelayedDrag)) : (H(d, "mouseup", i._disableDelayedDrag), H(d, "touchend", i._disableDelayedDrag), H(d, "touchcancel", i._disableDelayedDrag)), H(d, "mousemove", i._delayedDragTouchMoveHandler), H(d, "touchmove", i._delayedDragTouchMoveHandler), o.supportPointer && H(d, "pointermove", i._delayedDragTouchMoveHandler), i._dragStartTimer = setTimeout(c, o.delay);
      } else
        c();
    }
  },
  _delayedDragTouchMoveHandler: function(e) {
    var r = e.touches ? e.touches[0] : e;
    Math.max(Math.abs(r.clientX - this._lastX), Math.abs(r.clientY - this._lastY)) >= Math.floor(this.options.touchStartThreshold / (this.nativeDraggable && window.devicePixelRatio || 1)) && this._disableDelayedDrag();
  },
  _disableDelayedDrag: function() {
    g && tr(g), clearTimeout(this._dragStartTimer), this._disableDelayedDragEvents();
  },
  _disableDelayedDragEvents: function() {
    var e = this.el.ownerDocument;
    L(e, "mouseup", this._disableDelayedDrag), L(e, "touchend", this._disableDelayedDrag), L(e, "touchcancel", this._disableDelayedDrag), L(e, "pointerup", this._disableDelayedDrag), L(e, "pointercancel", this._disableDelayedDrag), L(e, "mousemove", this._delayedDragTouchMoveHandler), L(e, "touchmove", this._delayedDragTouchMoveHandler), L(e, "pointermove", this._delayedDragTouchMoveHandler);
  },
  _triggerDragStart: function(e, r) {
    r = r || e.pointerType == "touch" && e, !this.nativeDraggable || r ? this.options.supportPointer ? H(document, "pointermove", this._onTouchMove) : r ? H(document, "touchmove", this._onTouchMove) : H(document, "mousemove", this._onTouchMove) : (H(g, "dragend", this), H(j, "dragstart", this._onDragStart));
    try {
      document.selection ? wt(function() {
        document.selection.empty();
      }) : window.getSelection().removeAllRanges();
    } catch {
    }
  },
  _dragStarted: function(e, r) {
    if (Ze = !1, j && g) {
      oe("dragStarted", this, {
        evt: r
      }), this.nativeDraggable && H(document, "dragover", co);
      var n = this.options;
      !e && de(g, n.dragClass, !1), de(g, n.ghostClass, !0), $.active = this, e && this._appendGhost(), ne({
        sortable: this,
        name: "start",
        originalEvent: r
      });
    } else
      this._nulling();
  },
  _emulateDragOver: function() {
    if (pe) {
      this._lastX = pe.clientX, this._lastY = pe.clientY, zi();
      for (var e = document.elementFromPoint(pe.clientX, pe.clientY), r = e; e && e.shadowRoot && (e = e.shadowRoot.elementFromPoint(pe.clientX, pe.clientY), e !== r); )
        r = e;
      if (g.parentNode[le]._isOutsideThisEl(e), r)
        do {
          if (r[le]) {
            var n = void 0;
            if (n = r[le]._onDragOver({
              clientX: pe.clientX,
              clientY: pe.clientY,
              target: e,
              rootEl: r
            }), n && !this.options.dragoverBubble)
              break;
          }
          e = r;
        } while (r = qi(r));
      Ji();
    }
  },
  _onTouchMove: function(e) {
    if (Ne) {
      var r = this.options, n = r.fallbackTolerance, i = r.fallbackOffset, a = e.touches ? e.touches[0] : e, o = x && Xe(x, !0), d = x && o && o.a, c = x && o && o.d, s = mt && te && Pn(te), l = (a.clientX - Ne.clientX + i.x) / (d || 1) + (s ? s[0] - er[0] : 0) / (d || 1), u = (a.clientY - Ne.clientY + i.y) / (c || 1) + (s ? s[1] - er[1] : 0) / (c || 1);
      if (!$.active && !Ze) {
        if (n && Math.max(Math.abs(a.clientX - this._lastX), Math.abs(a.clientY - this._lastY)) < n)
          return;
        this._onDragStart(e, !0);
      }
      if (x) {
        o ? (o.e += l - (Kt || 0), o.f += u - (Qt || 0)) : o = {
          a: 1,
          b: 0,
          c: 0,
          d: 1,
          e: l,
          f: u
        };
        var h = "matrix(".concat(o.a, ",").concat(o.b, ",").concat(o.c, ",").concat(o.d, ",").concat(o.e, ",").concat(o.f, ")");
        S(x, "webkitTransform", h), S(x, "mozTransform", h), S(x, "msTransform", h), S(x, "transform", h), Kt = l, Qt = u, pe = a;
      }
      e.cancelable && e.preventDefault();
    }
  },
  _appendGhost: function() {
    if (!x) {
      var e = this.options.fallbackOnBody ? document.body : j, r = z(g, !0, mt, !0, e), n = this.options;
      if (mt) {
        for (te = e; S(te, "position") === "static" && S(te, "transform") === "none" && te !== document; )
          te = te.parentNode;
        te !== document.body && te !== document.documentElement ? (te === document && (te = ye()), r.top += te.scrollTop, r.left += te.scrollLeft) : te = ye(), er = Pn(te);
      }
      x = g.cloneNode(!0), de(x, n.ghostClass, !1), de(x, n.fallbackClass, !0), de(x, n.dragClass, !0), S(x, "transition", ""), S(x, "transform", ""), S(x, "box-sizing", "border-box"), S(x, "margin", 0), S(x, "top", r.top), S(x, "left", r.left), S(x, "width", r.width), S(x, "height", r.height), S(x, "opacity", "0.8"), S(x, "position", mt ? "absolute" : "fixed"), S(x, "zIndex", "100000"), S(x, "pointerEvents", "none"), $.ghost = x, e.appendChild(x), S(x, "transform-origin", _n / parseInt(x.style.width) * 100 + "% " + Ln / parseInt(x.style.height) * 100 + "%");
    }
  },
  _onDragStart: function(e, r) {
    var n = this, i = e.dataTransfer, a = n.options;
    if (oe("dragStart", this, {
      evt: e
    }), $.eventCanceled) {
      this._onDrop();
      return;
    }
    oe("setupClone", this), $.eventCanceled || (Z = Gi(g), Z.removeAttribute("id"), Z.draggable = !1, Z.style["will-change"] = "", this._hideClone(), de(Z, this.options.chosenClass, !1), $.clone = Z), n.cloneId = wt(function() {
      oe("clone", n), !$.eventCanceled && (n.options.removeCloneOnHide || j.insertBefore(Z, g), n._hideClone(), ne({
        sortable: n,
        name: "clone"
      }));
    }), !r && de(g, a.dragClass, !0), r ? (Ct = !0, n._loopId = setInterval(n._emulateDragOver, 50)) : (L(document, "mouseup", n._onDrop), L(document, "touchend", n._onDrop), L(document, "touchcancel", n._onDrop), i && (i.effectAllowed = "move", a.setData && a.setData.call(n, i, g)), H(document, "drop", n), S(g, "transform", "translateZ(0)")), Ze = !0, n._dragStartId = wt(n._dragStarted.bind(n, r, e)), H(document, "selectstart", n), Je = !0, window.getSelection().removeAllRanges(), tt && S(document.body, "user-select", "none");
  },
  // Returns true - if no further action is needed (either inserted or another condition)
  _onDragOver: function(e) {
    var r = this.el, n = e.target, i, a, o, d = this.options, c = d.group, s = $.active, l = ft === c, u = d.sort, h = Q || s, p, b = this, f = !1;
    if (en) return;
    function I(R, w) {
      oe(R, b, we({
        evt: e,
        isOwner: l,
        axis: p ? "vertical" : "horizontal",
        revert: o,
        dragRect: i,
        targetRect: a,
        canSort: u,
        fromSortable: h,
        target: n,
        completed: k,
        onMove: function(F, P) {
          return gt(j, r, g, i, F, z(F), e, P);
        },
        changed: A
      }, w));
    }
    function _() {
      I("dragOverAnimationCapture"), b.captureAnimationState(), b !== h && h.captureAnimationState();
    }
    function k(R) {
      return I("dragOverCompleted", {
        insertion: R
      }), R && (l ? s._hideClone() : s._showClone(b), b !== h && (de(g, Q ? Q.options.ghostClass : s.options.ghostClass, !1), de(g, d.ghostClass, !0)), Q !== b && b !== $.active ? Q = b : b === $.active && Q && (Q = null), h === b && (b._ignoreWhileAnimating = n), b.animateAll(function() {
        I("dragOverAnimationComplete"), b._ignoreWhileAnimating = null;
      }), b !== h && (h.animateAll(), h._ignoreWhileAnimating = null)), (n === g && !g.animated || n === r && !n.animated) && (Be = null), !d.dragoverBubble && !e.rootEl && n !== document && (g.parentNode[le]._isOutsideThisEl(e.target), !R && He(e)), !d.dragoverBubble && e.stopPropagation && e.stopPropagation(), f = !0;
    }
    function A() {
      ue = fe(g), Ce = fe(g, d.draggable), ne({
        sortable: b,
        name: "change",
        toEl: r,
        newIndex: ue,
        newDraggableIndex: Ce,
        originalEvent: e
      });
    }
    if (e.preventDefault !== void 0 && e.cancelable && e.preventDefault(), n = me(n, d.draggable, r, !0), I("dragOver"), $.eventCanceled) return f;
    if (g.contains(e.target) || n.animated && n.animatingX && n.animatingY || b._ignoreWhileAnimating === n)
      return k(!1);
    if (Ct = !1, s && !d.disabled && (l ? u || (o = U !== j) : Q === this || (this.lastPutMode = ft.checkPull(this, s, g, e)) && c.checkPut(this, s, g, e))) {
      if (p = this._getDirection(e, n) === "vertical", i = z(g), I("dragOverValid"), $.eventCanceled) return f;
      if (o)
        return U = j, _(), this._hideClone(), I("revert"), $.eventCanceled || (ke ? j.insertBefore(g, ke) : j.appendChild(g)), k(!0);
      var V = pn(r, d.draggable);
      if (!V || po(e, p, this) && !V.animated) {
        if (V === g)
          return k(!1);
        if (V && r === e.target && (n = V), n && (a = z(n)), gt(j, r, g, i, n, a, e, !!n) !== !1)
          return _(), V && V.nextSibling ? r.insertBefore(g, V.nextSibling) : r.appendChild(g), U = r, A(), k(!0);
      } else if (V && fo(e, p, this)) {
        var G = We(r, 0, d, !0);
        if (G === g)
          return k(!1);
        if (n = G, a = z(n), gt(j, r, g, i, n, a, e, !1) !== !1)
          return _(), r.insertBefore(g, G), U = r, A(), k(!0);
      } else if (n.parentNode === r) {
        a = z(n);
        var N = 0, q, T = g.parentNode !== r, J = !so(g.animated && g.toRect || i, n.animated && n.toRect || a, p), Re = p ? "top" : "left", he = An(n, "top", "top") || An(g, "top", "top"), Ie = he ? he.scrollTop : void 0;
        Be !== n && (q = a[Re], at = !1, pt = !J && d.invertSwap || T), N = mo(e, n, a, p, J ? 1 : d.swapThreshold, d.invertedSwapThreshold == null ? d.swapThreshold : d.invertedSwapThreshold, pt, Be === n);
        var ce;
        if (N !== 0) {
          var v = fe(g);
          do
            v -= N, ce = U.children[v];
          while (ce && (S(ce, "display") === "none" || ce === x));
        }
        if (N === 0 || ce === n)
          return k(!1);
        Be = n, it = N;
        var m = n.nextElementSibling, y = !1;
        y = N === 1;
        var E = gt(j, r, g, i, n, a, e, y);
        if (E !== !1)
          return (E === 1 || E === -1) && (y = E === 1), en = !0, setTimeout(ho, 30), _(), y && !m ? r.appendChild(g) : n.parentNode.insertBefore(g, y ? m : n), he && Zi(he, 0, Ie - he.scrollTop), U = g.parentNode, q !== void 0 && !pt && (yt = Math.abs(q - z(n)[Re])), A(), k(!0);
      }
      if (r.contains(g))
        return k(!1);
    }
    return !1;
  },
  _ignoreWhileAnimating: null,
  _offMoveEvents: function() {
    L(document, "mousemove", this._onTouchMove), L(document, "touchmove", this._onTouchMove), L(document, "pointermove", this._onTouchMove), L(document, "dragover", He), L(document, "mousemove", He), L(document, "touchmove", He);
  },
  _offUpEvents: function() {
    var e = this.el.ownerDocument;
    L(e, "mouseup", this._onDrop), L(e, "touchend", this._onDrop), L(e, "pointerup", this._onDrop), L(e, "pointercancel", this._onDrop), L(e, "touchcancel", this._onDrop), L(document, "selectstart", this);
  },
  _onDrop: function(e) {
    var r = this.el, n = this.options;
    if (ue = fe(g), Ce = fe(g, n.draggable), oe("drop", this, {
      evt: e
    }), U = g && g.parentNode, ue = fe(g), Ce = fe(g, n.draggable), $.eventCanceled) {
      this._nulling();
      return;
    }
    Ze = !1, pt = !1, at = !1, clearInterval(this._loopId), clearTimeout(this._dragStartTimer), tn(this.cloneId), tn(this._dragStartId), this.nativeDraggable && (L(document, "drop", this), L(r, "dragstart", this._onDragStart)), this._offMoveEvents(), this._offUpEvents(), tt && S(document.body, "user-select", ""), S(g, "transform", ""), e && (Je && (e.cancelable && e.preventDefault(), !n.dropBubble && e.stopPropagation()), x && x.parentNode && x.parentNode.removeChild(x), (j === U || Q && Q.lastPutMode !== "clone") && Z && Z.parentNode && Z.parentNode.removeChild(Z), g && (this.nativeDraggable && L(g, "dragend", this), tr(g), g.style["will-change"] = "", Je && !Ze && de(g, Q ? Q.options.ghostClass : this.options.ghostClass, !1), de(g, this.options.chosenClass, !1), ne({
      sortable: this,
      name: "unchoose",
      toEl: U,
      newIndex: null,
      newDraggableIndex: null,
      originalEvent: e
    }), j !== U ? (ue >= 0 && (ne({
      rootEl: U,
      name: "add",
      toEl: U,
      fromEl: j,
      originalEvent: e
    }), ne({
      sortable: this,
      name: "remove",
      toEl: U,
      originalEvent: e
    }), ne({
      rootEl: U,
      name: "sort",
      toEl: U,
      fromEl: j,
      originalEvent: e
    }), ne({
      sortable: this,
      name: "sort",
      toEl: U,
      originalEvent: e
    })), Q && Q.save()) : ue !== Ge && ue >= 0 && (ne({
      sortable: this,
      name: "update",
      toEl: U,
      originalEvent: e
    }), ne({
      sortable: this,
      name: "sort",
      toEl: U,
      originalEvent: e
    })), $.active && ((ue == null || ue === -1) && (ue = Ge, Ce = nt), ne({
      sortable: this,
      name: "end",
      toEl: U,
      originalEvent: e
    }), this.save()))), this._nulling();
  },
  _nulling: function() {
    oe("nulling", this), j = g = U = x = ke = Z = Et = De = Ne = pe = Je = ue = Ce = Ge = nt = Be = it = Q = ft = $.dragged = $.ghost = $.clone = $.active = null, Ot.forEach(function(e) {
      e.checked = !0;
    }), Ot.length = Kt = Qt = 0;
  },
  handleEvent: function(e) {
    switch (e.type) {
      case "drop":
      case "dragend":
        this._onDrop(e);
        break;
      case "dragenter":
      case "dragover":
        g && (this._onDragOver(e), uo(e));
        break;
      case "selectstart":
        e.preventDefault();
        break;
    }
  },
  /**
   * Serializes the item into an array of string.
   * @returns {String[]}
   */
  toArray: function() {
    for (var e = [], r, n = this.el.children, i = 0, a = n.length, o = this.options; i < a; i++)
      r = n[i], me(r, o.draggable, this.el, !1) && e.push(r.getAttribute(o.dataIdAttr) || vo(r));
    return e;
  },
  /**
   * Sorts the elements according to the array.
   * @param  {String[]}  order  order of the items
   */
  sort: function(e, r) {
    var n = {}, i = this.el;
    this.toArray().forEach(function(a, o) {
      var d = i.children[o];
      me(d, this.options.draggable, i, !1) && (n[a] = d);
    }, this), r && this.captureAnimationState(), e.forEach(function(a) {
      n[a] && (i.removeChild(n[a]), i.appendChild(n[a]));
    }), r && this.animateAll();
  },
  /**
   * Save the current sorting
   */
  save: function() {
    var e = this.options.store;
    e && e.set && e.set(this);
  },
  /**
   * For each element in the set, get the first element that matches the selector by testing the element itself and traversing up through its ancestors in the DOM tree.
   * @param   {HTMLElement}  el
   * @param   {String}       [selector]  default: `options.draggable`
   * @returns {HTMLElement|null}
   */
  closest: function(e, r) {
    return me(e, r || this.options.draggable, this.el, !1);
  },
  /**
   * Set/get option
   * @param   {string} name
   * @param   {*}      [value]
   * @returns {*}
   */
  option: function(e, r) {
    var n = this.options;
    if (r === void 0)
      return n[e];
    var i = ct.modifyOption(this, e, r);
    typeof i < "u" ? n[e] = i : n[e] = r, e === "group" && Yi(n);
  },
  /**
   * Destroy
   */
  destroy: function() {
    oe("destroy", this);
    var e = this.el;
    e[le] = null, L(e, "mousedown", this._onTapStart), L(e, "touchstart", this._onTapStart), L(e, "pointerdown", this._onTapStart), this.nativeDraggable && (L(e, "dragover", this), L(e, "dragenter", this)), Array.prototype.forEach.call(e.querySelectorAll("[draggable]"), function(r) {
      r.removeAttribute("draggable");
    }), this._onDrop(), this._disableDelayedDragEvents(), Dt.splice(Dt.indexOf(this.el), 1), this.el = e = null;
  },
  _hideClone: function() {
    if (!De) {
      if (oe("hideClone", this), $.eventCanceled) return;
      S(Z, "display", "none"), this.options.removeCloneOnHide && Z.parentNode && Z.parentNode.removeChild(Z), De = !0;
    }
  },
  _showClone: function(e) {
    if (e.lastPutMode !== "clone") {
      this._hideClone();
      return;
    }
    if (De) {
      if (oe("showClone", this), $.eventCanceled) return;
      g.parentNode == j && !this.options.group.revertClone ? j.insertBefore(Z, g) : ke ? j.insertBefore(Z, ke) : j.appendChild(Z), this.options.group.revertClone && this.animate(g, Z), S(Z, "display", ""), De = !1;
    }
  }
};
function uo(t) {
  t.dataTransfer && (t.dataTransfer.dropEffect = "move"), t.cancelable && t.preventDefault();
}
function gt(t, e, r, n, i, a, o, d) {
  var c, s = t[le], l = s.options.onMove, u;
  return window.CustomEvent && !xe && !lt ? c = new CustomEvent("move", {
    bubbles: !0,
    cancelable: !0
  }) : (c = document.createEvent("Event"), c.initEvent("move", !0, !0)), c.to = e, c.from = t, c.dragged = r, c.draggedRect = n, c.related = i || e, c.relatedRect = a || z(e), c.willInsertAfter = d, c.originalEvent = o, t.dispatchEvent(c), l && (u = l.call(s, c, o)), u;
}
function tr(t) {
  t.draggable = !1;
}
function ho() {
  en = !1;
}
function fo(t, e, r) {
  var n = z(We(r.el, 0, r.options, !0)), i = Ui(r.el, r.options, x), a = 10;
  return e ? t.clientX < i.left - a || t.clientY < n.top && t.clientX < n.right : t.clientY < i.top - a || t.clientY < n.bottom && t.clientX < n.left;
}
function po(t, e, r) {
  var n = z(pn(r.el, r.options.draggable)), i = Ui(r.el, r.options, x), a = 10;
  return e ? t.clientX > i.right + a || t.clientY > n.bottom && t.clientX > n.left : t.clientY > i.bottom + a || t.clientX > n.right && t.clientY > n.top;
}
function mo(t, e, r, n, i, a, o, d) {
  var c = n ? t.clientY : t.clientX, s = n ? r.height : r.width, l = n ? r.top : r.left, u = n ? r.bottom : r.right, h = !1;
  if (!o) {
    if (d && yt < s * i) {
      if (!at && (it === 1 ? c > l + s * a / 2 : c < u - s * a / 2) && (at = !0), at)
        h = !0;
      else if (it === 1 ? c < l + yt : c > u - yt)
        return -it;
    } else if (c > l + s * (1 - i) / 2 && c < u - s * (1 - i) / 2)
      return go(e);
  }
  return h = h || o, h && (c < l + s * a / 2 || c > u - s * a / 2) ? c > l + s / 2 ? 1 : -1 : 0;
}
function go(t) {
  return fe(g) < fe(t) ? 1 : -1;
}
function vo(t) {
  for (var e = t.tagName + t.className + t.src + t.href + t.textContent, r = e.length, n = 0; r--; )
    n += e.charCodeAt(r);
  return n.toString(36);
}
function bo(t) {
  Ot.length = 0;
  for (var e = t.getElementsByTagName("input"), r = e.length; r--; ) {
    var n = e[r];
    n.checked && Ot.push(n);
  }
}
function wt(t) {
  return setTimeout(t, 0);
}
function tn(t) {
  return clearTimeout(t);
}
Vt && H(document, "touchmove", function(t) {
  ($.active || Ze) && t.cancelable && t.preventDefault();
});
$.utils = {
  on: H,
  off: L,
  css: S,
  find: Bi,
  is: function(e, r) {
    return !!me(e, r, e, !1);
  },
  extend: Qa,
  throttle: ji,
  closest: me,
  toggleClass: de,
  clone: Gi,
  index: fe,
  nextTick: wt,
  cancelNextTick: tn,
  detectDirection: Wi,
  getChild: We,
  expando: le
};
$.get = function(t) {
  return t[le];
};
$.mount = function() {
  for (var t = arguments.length, e = new Array(t), r = 0; r < t; r++)
    e[r] = arguments[r];
  e[0].constructor === Array && (e = e[0]), e.forEach(function(n) {
    if (!n.prototype || !n.prototype.constructor)
      throw "Sortable: Mounted plugin must be a constructor function, not ".concat({}.toString.call(n));
    n.utils && ($.utils = we(we({}, $.utils), n.utils)), ct.mount(n);
  });
};
$.create = function(t, e) {
  return new $(t, e);
};
$.version = Ja;
var Y = [], Ke, rn, nn = !1, rr, nr, At, Qe;
function Eo() {
  function t() {
    this.defaults = {
      scroll: !0,
      forceAutoScrollFallback: !1,
      scrollSensitivity: 30,
      scrollSpeed: 10,
      bubbleScroll: !0
    };
    for (var e in this)
      e.charAt(0) === "_" && typeof this[e] == "function" && (this[e] = this[e].bind(this));
  }
  return t.prototype = {
    dragStarted: function(r) {
      var n = r.originalEvent;
      this.sortable.nativeDraggable ? H(document, "dragover", this._handleAutoScroll) : this.options.supportPointer ? H(document, "pointermove", this._handleFallbackAutoScroll) : n.touches ? H(document, "touchmove", this._handleFallbackAutoScroll) : H(document, "mousemove", this._handleFallbackAutoScroll);
    },
    dragOverCompleted: function(r) {
      var n = r.originalEvent;
      !this.options.dragOverBubble && !n.rootEl && this._handleAutoScroll(n);
    },
    drop: function() {
      this.sortable.nativeDraggable ? L(document, "dragover", this._handleAutoScroll) : (L(document, "pointermove", this._handleFallbackAutoScroll), L(document, "touchmove", this._handleFallbackAutoScroll), L(document, "mousemove", this._handleFallbackAutoScroll)), Nn(), Rt(), eo();
    },
    nulling: function() {
      At = rn = Ke = nn = Qe = rr = nr = null, Y.length = 0;
    },
    _handleFallbackAutoScroll: function(r) {
      this._handleAutoScroll(r, !0);
    },
    _handleAutoScroll: function(r, n) {
      var i = this, a = (r.touches ? r.touches[0] : r).clientX, o = (r.touches ? r.touches[0] : r).clientY, d = document.elementFromPoint(a, o);
      if (At = r, n || this.options.forceAutoScrollFallback || lt || xe || tt) {
        ir(r, this.options, d, n);
        var c = Ae(d, !0);
        nn && (!Qe || a !== rr || o !== nr) && (Qe && Nn(), Qe = setInterval(function() {
          var s = Ae(document.elementFromPoint(a, o), !0);
          s !== c && (c = s, Rt()), ir(r, i.options, s, n);
        }, 10), rr = a, nr = o);
      } else {
        if (!this.options.bubbleScroll || Ae(d, !0) === ye()) {
          Rt();
          return;
        }
        ir(r, this.options, Ae(d, !1), !1);
      }
    }
  }, $e(t, {
    pluginName: "scroll",
    initializeByDefault: !0
  });
}
function Rt() {
  Y.forEach(function(t) {
    clearInterval(t.pid);
  }), Y = [];
}
function Nn() {
  clearInterval(Qe);
}
var ir = ji(function(t, e, r, n) {
  if (e.scroll) {
    var i = (t.touches ? t.touches[0] : t).clientX, a = (t.touches ? t.touches[0] : t).clientY, o = e.scrollSensitivity, d = e.scrollSpeed, c = ye(), s = !1, l;
    rn !== r && (rn = r, Rt(), Ke = e.scroll, l = e.scrollFn, Ke === !0 && (Ke = Ae(r, !0)));
    var u = 0, h = Ke;
    do {
      var p = h, b = z(p), f = b.top, I = b.bottom, _ = b.left, k = b.right, A = b.width, V = b.height, G = void 0, N = void 0, q = p.scrollWidth, T = p.scrollHeight, J = S(p), Re = p.scrollLeft, he = p.scrollTop;
      p === c ? (G = A < q && (J.overflowX === "auto" || J.overflowX === "scroll" || J.overflowX === "visible"), N = V < T && (J.overflowY === "auto" || J.overflowY === "scroll" || J.overflowY === "visible")) : (G = A < q && (J.overflowX === "auto" || J.overflowX === "scroll"), N = V < T && (J.overflowY === "auto" || J.overflowY === "scroll"));
      var Ie = G && (Math.abs(k - i) <= o && Re + A < q) - (Math.abs(_ - i) <= o && !!Re), ce = N && (Math.abs(I - a) <= o && he + V < T) - (Math.abs(f - a) <= o && !!he);
      if (!Y[u])
        for (var v = 0; v <= u; v++)
          Y[v] || (Y[v] = {});
      (Y[u].vx != Ie || Y[u].vy != ce || Y[u].el !== p) && (Y[u].el = p, Y[u].vx = Ie, Y[u].vy = ce, clearInterval(Y[u].pid), (Ie != 0 || ce != 0) && (s = !0, Y[u].pid = setInterval((function() {
        n && this.layer === 0 && $.active._onTouchMove(At);
        var m = Y[this.layer].vy ? Y[this.layer].vy * d : 0, y = Y[this.layer].vx ? Y[this.layer].vx * d : 0;
        typeof l == "function" && l.call($.dragged.parentNode[le], y, m, t, At, Y[this.layer].el) !== "continue" || Zi(Y[this.layer].el, y, m);
      }).bind({
        layer: u
      }), 24))), u++;
    } while (e.bubbleScroll && h !== c && (h = Ae(h, !1)));
    nn = s;
  }
}, 30), Ki = function(e) {
  var r = e.originalEvent, n = e.putSortable, i = e.dragEl, a = e.activeSortable, o = e.dispatchSortableEvent, d = e.hideGhostForTarget, c = e.unhideGhostForTarget;
  if (r) {
    var s = n || a;
    d();
    var l = r.changedTouches && r.changedTouches.length ? r.changedTouches[0] : r, u = document.elementFromPoint(l.clientX, l.clientY);
    c(), s && !s.el.contains(u) && (o("spill"), this.onSpill({
      dragEl: i,
      putSortable: n
    }));
  }
};
function mn() {
}
mn.prototype = {
  startIndex: null,
  dragStart: function(e) {
    var r = e.oldDraggableIndex;
    this.startIndex = r;
  },
  onSpill: function(e) {
    var r = e.dragEl, n = e.putSortable;
    this.sortable.captureAnimationState(), n && n.captureAnimationState();
    var i = We(this.sortable.el, this.startIndex, this.options);
    i ? this.sortable.el.insertBefore(r, i) : this.sortable.el.appendChild(r), this.sortable.animateAll(), n && n.animateAll();
  },
  drop: Ki
};
$e(mn, {
  pluginName: "revertOnSpill"
});
function gn() {
}
gn.prototype = {
  onSpill: function(e) {
    var r = e.dragEl, n = e.putSortable, i = n || this.sortable;
    i.captureAnimationState(), r.parentNode && r.parentNode.removeChild(r), i.animateAll();
  },
  drop: Ki
};
$e(gn, {
  pluginName: "removeOnSpill"
});
$.mount(new Eo());
$.mount(gn, mn);
const an = 0.4;
class yo {
  constructor() {
    this.handlers = [], this.toolbarConnected = (e) => {
      this.toolbar = e, this.reactionDisposer = st(
        () => je.getToolbarExpandMode(),
        (r, n) => {
          if (!this.toolbar)
            return;
          if (n) {
            const a = this.handlers.find((o) => o.getMode() === n);
            a && a.onDeactivate(this.toolbar);
          }
          const i = this.handlers.find((a) => a.getMode() === r);
          i && (this.activeExpandModeHandler = i, this.activeExpandModeHandler.onActivate(this.toolbar));
        },
        { fireImmediately: !0 }
      );
    }, this.toolbarDisconnected = () => {
      this.activeExpandModeHandler && this.toolbar && this.activeExpandModeHandler.onDeactivate(this.toolbar), this.reactionDisposer && this.reactionDisposer(), this.toolbar = void 0;
    }, this.devToolsPopoverOpenClosedChanged = (e) => {
      this.activeExpandModeHandler && this.activeExpandModeHandler.devToolsPopoverOpenClosedChanged(e);
    }, this.handlers.push(new on()), this.handlers.push(new wo()), this.handlers.push(new Ro()), this.handlers.push(new Io()), this.handlers.push(new So());
  }
}
class dt {
  constructor() {
    this.isDevToolsPopoverOpen = !1;
  }
  devToolsPopoverOpenClosedChanged(e) {
    this.isDevToolsPopoverOpen = e;
  }
  getIdleWidth(e) {
    const r = e.radioGroup;
    return Number.parseFloat(getComputedStyle(r).fontSize) * 0.125 * 2 + 32;
  }
  shrink() {
    this.toolbar && (this.isDevToolsPopoverOpen || (this.updateToolbarOpacity(!0), this.toolbar.setPlayModeIconProgress(0), this.toolbar.radioGroup.style.width = `${this.getIdleWidth(this.toolbar)}px`, this.toolbar.updateToolbarTotalWidth(), this.toolbar.removeAttribute("modes-expanded")));
  }
  updateToolbarOpacity(e) {
    this.toolbar && (e ? this.toolbar.style.opacity = `${an}` : this.toolbar.style.opacity = "1");
  }
}
const Pt = class Pt extends dt {
  constructor() {
    super(...arguments), this.lastDocumentMouseClientX = 0, this.lastDocumentMouseClientY = 0, this.ticking = !1, this.handlePointerProximity = (e) => {
      this.toolbar && (this.lastDocumentMouseClientX = e.clientX, this.lastDocumentMouseClientY = e.clientY, this.ticking || (requestAnimationFrame(() => {
        const r = this.toolbar;
        if (r) {
          if (r.isDragging || r.matches(":hover") || r.matches(":focus-within") || this.isDevToolsPopoverOpen) {
            r.expandRadioButtons(), this.ticking = !1;
            return;
          }
          if (O.activeMode !== "play") {
            r.expandRadioButtons(), this.ticking = !1;
            return;
          }
          this.updateRadioButtonExpandAndStylingByPointerLocation(e.clientX, e.clientY), this.ticking = !1;
        }
      }), this.ticking = !0));
    }, this.updateRadioButtonExpandAndStylingByPointerLocation = (e, r) => {
      if (!this.toolbar)
        return;
      if (O.activeMode !== "play" && !this.toolbar.hasAttribute("modes-expanded")) {
        this.toolbar.expandRadioButtons();
        return;
      }
      const n = this.toolbar.radioGroup, i = n.style.width, a = this.toolbar.getBoundingClientRect(), o = Math.max(a.left - e, 0, e - a.right), d = Math.max(a.top - r, 0, r - a.bottom), c = Math.hypot(o, d), s = Math.max(0, 1 - c / Pt.PROXIMITY_RADIUS), l = Number.parseFloat(getComputedStyle(n).getPropertyValue("--expanded-full-width")), u = Number.parseFloat(getComputedStyle(n).fontSize) * 0.125 * 2 + 32, h = an + (1 - an) * s;
      this.toolbar.style.opacity = h.toFixed(2), this.toolbar.setPlayModeIconProgress(s);
      const p = u + (l - u) * s;
      n.style.width = `${p.toFixed(2)}px`, i !== n.style.width && this.toolbar.updateToolbarTotalWidth(), this.toolbar.toggleAttribute("modes-expanded", p === l);
    };
  }
  onActivate(e) {
    this.toolbar = e, this.activeModeReactionDisposer = st(
      () => O.activeMode,
      () => {
        O.activeMode !== "play" && this.toolbar ? (this.updateToolbarOpacity(!1), this.toolbar.expandRadioButtons()) : this.updateRadioButtonExpandAndStylingByPointerLocation(
          this.lastDocumentMouseClientX,
          this.lastDocumentMouseClientY
        );
      },
      { fireImmediately: !0 }
    ), document.addEventListener("mousemove", this.handlePointerProximity);
  }
  getMode() {
    return "proximity";
  }
  onDeactivate(e) {
    document.removeEventListener("mousemove", this.handlePointerProximity), this.activeModeReactionDisposer && this.activeModeReactionDisposer();
  }
  devToolsPopoverOpenClosedChanged(e) {
    super.devToolsPopoverOpenClosedChanged(e), this.isDevToolsPopoverOpen || this.updateRadioButtonExpandAndStylingByPointerLocation(
      this.lastDocumentMouseClientX,
      this.lastDocumentMouseClientY
    );
  }
};
Pt.PROXIMITY_RADIUS = 180;
let on = Pt;
class wo extends dt {
  constructor() {
    super(...arguments), this.mouseLeft = !1, this.handleMouseOverEvent = (e) => {
      this.toolbar && (this.mouseLeft = !1, this.toolbar.expandRadioButtons(), this.toolbar.updateToolbarTotalWidth());
    }, this.handleMouseLeaveEvent = (e) => {
      this.mouseLeft = !0, O.activeMode === "play" && this.shrink();
    };
  }
  getMode() {
    return "hover";
  }
  onActivate(e) {
    this.toolbar = e, e.addEventListener("mouseover", this.handleMouseOverEvent), e.addEventListener("mouseleave", this.handleMouseLeaveEvent), this.activeModeReactionDisposer = st(
      () => O.activeMode,
      () => {
        this.updateByCopilotActiveMode();
      },
      { fireImmediately: !0 }
    );
  }
  updateByCopilotActiveMode() {
    O.activeMode === "play" ? this.shrink() : this.toolbar?.expandRadioButtons();
  }
  onDeactivate(e) {
    this.toolbar = e, e.removeEventListener("mouseover", this.handleMouseOverEvent), e.removeEventListener("mouseleave", this.handleMouseLeaveEvent);
  }
  devToolsPopoverOpenClosedChanged(e) {
    super.devToolsPopoverOpenClosedChanged(e), this.mouseLeft && this.shrink();
  }
}
class Ro extends dt {
  constructor() {
    super(...arguments), this.onPlayClickAction = "expand", this.handleClick = (e) => {
      if (O.activeMode === "play" && e.target && e.target instanceof HTMLElement) {
        const n = e.target.closest("vaadin-radio-button");
        n?.dataset.mode && (n.dataset.mode === "play" ? this.onPlayClickAction === "expand" ? (this.toolbar?.expandRadioButtons(), this.onPlayClickAction = "collapse") : (this.shrink(), this.onPlayClickAction = "expand") : (this.toolbar?.expandRadioButtons(), this.onPlayClickAction = "collapse"));
      }
    };
  }
  getMode() {
    return "click";
  }
  onActivate(e) {
    this.toolbar = e, e.addEventListener("click", this.handleClick), requestAnimationFrame(() => {
      this.updateByCopilotActiveMode();
    }), this.activeModeReactionDisposer = st(
      () => O.activeMode,
      () => {
        this.updateByCopilotActiveMode();
      },
      { fireImmediately: !0 }
    );
  }
  onDeactivate(e) {
    e.removeEventListener("click", this.handleClick), this.toolbar = void 0, this.activeModeReactionDisposer && this.activeModeReactionDisposer();
  }
  updateByCopilotActiveMode() {
    O.activeMode === "play" ? (this.shrink(), this.onPlayClickAction = "expand") : this.toolbar?.expandRadioButtons();
  }
  devToolsPopoverOpenClosedChanged(e) {
    super.devToolsPopoverOpenClosedChanged(e), this.updateToolbarOpacity(!e);
  }
}
class Io extends dt {
  getMode() {
    return "always";
  }
  onActivate(e) {
    this.toolbar = e, e.expandRadioButtons();
  }
  onDeactivate(e) {
  }
}
class So extends dt {
  constructor() {
    super(...arguments), this.handleMouseOverListener = (e) => {
      this.toolbar && this.updateToolbarOpacity(!1);
    }, this.handleMouseLeaveListener = (e) => {
      this.toolbar && this.updateToolbarOpacity(!0);
    };
  }
  getMode() {
    return "never";
  }
  onActivate(e) {
    this.toolbar = e, O.setActiveMode("play", !0), e.addEventListener("mouseover", this.handleMouseOverListener), e.addEventListener("mouseleave", this.handleMouseLeaveListener), this.shrink();
  }
  onDeactivate(e) {
    this.toolbar && (e.removeEventListener("mouseover", this.handleMouseOverListener), e.removeEventListener("mouseleave", this.handleMouseLeaveListener));
  }
}
var $o = Object.defineProperty, To = Object.getOwnPropertyDescriptor, Ve = (t, e, r, n) => {
  for (var i = n > 1 ? void 0 : n ? To(e, r) : e, a = t.length - 1, o; a >= 0; a--)
    (o = t[a]) && (i = (n ? o(e, r, i) : o(i)) || i);
  return n && i && $o(e, r, i), i;
};
let ie = class extends _t {
  constructor() {
    super(...arguments), this.sortableInitialized = !1, this.modeRadioButtonItems = [], this.devToolsDataLoading = !1, this.devToolsDataLoaded = !1, this.isDragging = !1, this.dragPreparing = !1, this.dragStartX = 0, this.dragStartY = 0, this.toolbarStartRight = 0, this.toolbarStartTop = 0, this.wasDraggedInLastInteraction = !1, this.dragThreshold = 5, this.expandModeHandler = new yo(), this.transitionStart = (t) => {
      t.propertyName === "width" && this.setAttribute(ie.TRANSITION_STATUS_ATTR_KEY, "started");
    }, this.transitionEnd = (t) => {
      t.propertyName === "width" && (this.setAttribute(ie.TRANSITION_STATUS_ATTR_KEY, "ended"), this.constrainToViewport());
    }, this.handleMouseDown = (t) => {
      if (t.button !== 0)
        return;
      const e = this.querySelector("#devtools-button");
      if (!e || !(t.target instanceof Node) || !e.contains(t.target))
        return;
      this.dragPreparing = !0, this.dragStartX = t.clientX, this.dragStartY = t.clientY;
      const r = this.getBoundingClientRect();
      this.toolbarStartRight = window.innerWidth - r.right, this.toolbarStartTop = r.top, document.addEventListener("mousemove", this.handleMouseMove), document.addEventListener("mouseup", this.handleMouseUp);
    }, this.handleMouseMove = (t) => {
      if (!this.dragPreparing && !this.isDragging)
        return;
      const e = t.clientX - this.dragStartX, r = t.clientY - this.dragStartY, n = Math.sqrt(e * e + r * r);
      if (this.dragPreparing && n > this.dragThreshold && (this.isDragging = !0, this.dragPreparing = !1, this.style.transition = "none"), this.isDragging) {
        let i = this.toolbarStartRight - e, a = this.toolbarStartTop + r;
        const o = this.getBoundingClientRect(), d = o.width, c = o.height;
        i = Math.max(0, i), window.innerWidth - i - d < 0 && (i = window.innerWidth - d), a = Math.max(0, a);
        const l = window.innerHeight - c;
        a = Math.min(a, l), this.style.right = `${i}px`, this.style.top = `${a}px`, this.style.bottom = "auto", this.style.left = "auto";
      }
    }, this.handleMouseUp = () => {
      const t = this.isDragging;
      if (this.isDragging = !1, this.dragPreparing = !1, this.wasDraggedInLastInteraction = t, t) {
        this.style.transition = "";
        const e = this.getBoundingClientRect(), r = window.innerWidth - e.right, n = e.top;
        Wt.saveToolbarPosition(r, n);
      }
      document.removeEventListener("mousemove", this.handleMouseMove), document.removeEventListener("mouseup", this.handleMouseUp), setTimeout(() => {
        this.wasDraggedInLastInteraction = !1;
      }, 10);
    }, this.handleClick = (t) => {
      this.wasDraggedInLastInteraction && (t.stopPropagation(), t.preventDefault());
    }, this.handleWindowResize = () => {
      this.constrainToViewport(!0);
    }, this.expandRadioButtons = () => {
      if (this.hasAttribute("modes-expanded"))
        return;
      const t = this.radioGroup.style.width;
      this.style.opacity = "1", this.setPlayModeIconProgress(1);
      const e = Number.parseFloat(getComputedStyle(this.radioGroup).getPropertyValue("--expanded-full-width"));
      this.radioGroup.style.width = `${e.toFixed(2)}px`, t !== this.radioGroup.style.width && this.updateToolbarTotalWidth(), this.toggleAttribute("modes-expanded", !0);
    }, this.onSortEndEvent = (t, e, r) => {
      if (!(t.item instanceof HTMLElement))
        return;
      const n = t.item.dataset.panelTag;
      if (!n || !M.getPanelByTag(n))
        return;
      const a = this.getOrderByTag(e);
      M.reOrderPanels(a, r);
    };
  }
  connectedCallback() {
    super.connectedCallback(), this.popover = "manual", this.setPlayModeIconProgress(1), this.modeRadioButtonItems = Object.entries(ba).map(([e, r]) => ({
      value: e,
      label: r.label,
      order: r.toolbarOrder,
      icon: r.toolbarIcon
    })), this.modeRadioButtonItems.sort((e, r) => e.order - r.order), this.classList.add(
      "backdrop-blur-sm",
      "bg-gray-1",
      "dark:bg-gray-5",
      "border-0",
      "bottom-4",
      "duration-300",
      "end-4",
      "fixed",
      "flex",
      "justify-end",
      "m-0",
      "overflow-hidden",
      "p-1",
      "pointer-events-auto",
      "rounded-xl",
      "shadow-xl",
      "start-auto",
      "top-auto",
      "transition-all",
      "z-100"
    );
    const t = Wt.getToolbarPosition();
    if (t) {
      const e = t.right, r = t.top;
      this.style.right = `${e}px`, this.style.top = `${r}px`, this.style.bottom = "auto", this.style.left = "auto";
    }
    this.addEventListener("transitionstart", this.transitionStart), this.addEventListener("transitionend", this.transitionEnd), this.addEventListener("mousedown", this.handleMouseDown), this.addEventListener("click", this.handleClick, !0), window.addEventListener("resize", this.handleWindowResize), this.reaction(
      () => O.userInfo,
      () => {
        O.userInfo && this.devToolsPopover.requestContentUpdate();
      }
    ), this.reaction(
      () => O.activeMode,
      () => {
        this.queryModeRadioButtons().forEach((r) => {
          const n = r;
          n._setFocused && n._setFocused(!1);
        });
        const e = this.queryRadioButtonForMode(O.activeMode);
        e?._setFocused && e._setFocused(!0);
      }
    );
  }
  disconnectedCallback() {
    super.disconnectedCallback(), this.removeEventListener("transitionstart", this.transitionStart), this.removeEventListener("transitionend", this.transitionEnd), this.removeEventListener("mousedown", this.handleMouseDown), this.removeEventListener("click", this.handleClick, !0), document.removeEventListener("mousemove", this.handleMouseMove), document.removeEventListener("mouseup", this.handleMouseUp), window.removeEventListener("resize", this.handleWindowResize), this.expandModeHandler.toolbarDisconnected();
  }
  constrainToViewport(t = !1) {
    if (!this.style.right || this.style.right === "")
      return;
    const e = this.getBoundingClientRect(), r = e.width, n = e.height;
    if (r === 0 || n === 0)
      return;
    let i = parseFloat(this.style.right), a = parseFloat(this.style.top || "0");
    const o = i, d = a;
    i = Math.max(0, i), window.innerWidth - i - r < 0 && (i = window.innerWidth - r), a = Math.max(0, a);
    const s = window.innerHeight - n;
    a = Math.min(a, s), this.style.right = `${i}px`, this.style.top = `${a}px`, t && (i !== o || a !== d) && Wt.saveToolbarPosition(i, a);
  }
  createRenderRoot() {
    return this;
  }
  firstUpdated(t) {
    super.firstUpdated(t), this.expandModeHandler.toolbarConnected(this);
  }
  updated(t) {
    super.updated(t), this.updateToolbarTotalWidth(), this.modePanelIconContainer !== null && !this.sortableInitialized && (new $(this.modePanelIconContainer, {
      animation: 150,
      draggable: ".panel-icon-button",
      onEnd: (e) => this.onSortEndEvent(e, this.modePanelIconContainer, O.activeMode)
    }), this.sortableInitialized = !0);
  }
  updateToolbarTotalWidth() {
    this.classList.add("transition-none");
    const e = [...this.querySelectorAll(
      "#mode-panel-icon-container, #mode-icon-container, #common-mode-panel-icon-container"
    )].reduce((r, n) => r + n.offsetWidth, 0);
    this.style.width = `${e}px`, this.classList.remove("transition-none");
  }
  setPlayModeIconProgress(t) {
    const e = Math.min(1, Math.max(0, t));
    this.style.setProperty(ie.PLAY_MODE_ICON_PROGRESS_CSS_VAR, e.toFixed(2));
  }
  render() {
    const t = M.panels;
    return C`
      <style>
        .toolbar-play-mode-icon-stack {
          display: grid;
          place-items: center;
        }

        .toolbar-play-mode-icon-stack > vaadin-icon {
          grid-area: 1 / 1;
          transition: opacity 300ms ease;
        }

        .toolbar-play-mode-icon--play {
          opacity: var(${ie.PLAY_MODE_ICON_PROGRESS_CSS_VAR}, 1);
        }

        .toolbar-play-mode-icon--vaadin {
          opacity: calc(1 - var(${ie.PLAY_MODE_ICON_PROGRESS_CSS_VAR}, 1));
        }
      </style>
      <div class="flex pe-1" id="mode-panel-icon-container" ?hidden="${O.activeMode === "play"}">
        ${O.activeMode ? this.renderPanelList(t, O.activeMode) : Oe}
      </div>
      <div class="flex pe-1" id="mode-icon-container">${this.renderCopilotModeButtons()}</div>
      <div class="flex" id="common-mode-panel-icon-container">
        <vaadin-button aria-label="DevTools" class="max-w-6 min-w-0" id="devtools-button" theme="icon tertiary toolbar">
          <vaadin-icon .svg="${ee.moreVert}"></vaadin-icon>
          <vaadin-tooltip slot="tooltip" text="Open menu"></vaadin-tooltip>
        </vaadin-button>
        <vaadin-popover
          @opened-changed="${(e) => {
      this.expandModeHandler.devToolsPopoverOpenClosedChanged(e.detail.value), e.detail.value && !this.devToolsDataLoaded && (this.devToolsDataLoading = !0, import("./copilot-devtools-C6iQ1Fwq.js").then(() => {
        Ea(), M.restorePanelsFromStorage(), ya(), wa(), this.devToolsDataLoading = !1, this.devToolsDataLoaded = !0;
      }));
    }}"
          position="top"
          for="devtools-button"
          id="devtools-popover"
          modal
          theme="arrow no-padding"
          width="360">
          <!--          TODO move loader here so it should not wait for devtools to be imported -->
          <copilot-devtools .loading="${this.devToolsDataLoading}"></copilot-devtools>
        </vaadin-popover>
      </div>
    `;
  }
  renderPanelList(t, e) {
    return C`
      ${et(
      t.filter((r) => r.experimental?.enabled() !== !1).filter((r) => r.toolbarOptions?.allowedModesWithOrder?.[e] !== void 0).sort((r, n) => r.toolbarOptions.allowedModesWithOrder[e] - n.toolbarOptions.allowedModesWithOrder[e]),
      (r) => r.tag,
      (r) => this.renderPanelIcon(r)
    )}
    `;
  }
  renderCopilotModeButtons() {
    return C`
      <vaadin-radio-group
        theme="toolbar"
        .value="${O.activeMode}"
        @change="${(t) => {
      const e = t.target.value;
      O.setActiveMode(e, !0);
    }}">
        ${et(
      this.modeRadioButtonItems,
      (t) => t.value,
      (t) => C`
            <vaadin-radio-button data-mode="${t.value}" .value="${t.value}" .id="${t.value}-mode-radio-btn">
              <label slot="label">${this.renderModeButtonIcon(t)}</label>
            </vaadin-radio-button>
          `
    )}
      </vaadin-radio-group>
      ${et(
      this.modeRadioButtonItems,
      (t) => t.value,
      (t) => C`
          <vaadin-tooltip for="${t.value}-mode-radio-btn" text="${t.label} Mode"></vaadin-tooltip>
        `
    )}
    `;
  }
  renderModeButtonIcon(t) {
    return t.value !== "play" ? C`<vaadin-icon .svg="${ee[t.icon]}"></vaadin-icon>` : C`
      <span aria-hidden="true" class="toolbar-play-mode-icon-stack">
        <vaadin-icon class="toolbar-play-mode-icon--play" .svg="${ee[t.icon]}"></vaadin-icon>
        <vaadin-icon class="toolbar-play-mode-icon--vaadin" .svg="${ee.vaadin}"></vaadin-icon>
      </span>
    `;
  }
  renderPanelIcon(t) {
    const e = this.getButtonId(t);
    return C`
      <vaadin-button
        aria-expanded="${M.isOpenedPanel(t.tag)}"
        aria-label="${t.header}"
        class="panel-icon-button relative"
        data-panel-tag="${t.tag}"
        id="${e}"
        theme="icon tertiary toolbar"
        @click=${(r) => {
      if (M.isOpenedPanel(t.tag)) {
        const o = this.getRootNode().querySelector("copilot-panel-manager")?.getDialogByPanelTag(t.tag)?.querySelector(t.tag);
        o?.requestClose ? o.requestClose(() => M.closePanel(t.tag)) : M.closePanel(t.tag);
      } else
        M.attentionRequiredPanelTag === t.tag && M.clearAttention(), M.openPanel(t.tag);
    }}>
        <vaadin-icon .svg="${ee[t.toolbarOptions.iconKey]}"></vaadin-icon>
        <vaadin-tooltip
          slot="tooltip"
          text="${t.header}${M.attentionRequiredPanelTag === t.tag ? " – Attention Required" : ""}"></vaadin-tooltip>
        ${M.attentionRequiredPanelTag === t.tag ? C`<span
                aria-hidden="true"
                class="absolute animate-ping bg-amber-11 end-0.5 rounded-full size-2 top-0.5"></span>
              <span
                aria-hidden="true"
                class="absolute bg-amber-11 border border-gray-1 dark:border-gray-5 box-border end-0.5 rounded-full size-2 top-0.5"></span>` : Oe}
      </vaadin-button>
    `;
  }
  getButtonId(t) {
    return `${t.tag}-toolbar-btn`;
  }
  getOrderByTag(t) {
    const e = /* @__PURE__ */ new Map();
    return [...t.querySelectorAll("[data-panel-tag]")].forEach((r, n) => {
      e.set(r.dataset.panelTag, n);
    }), e;
  }
  queryModeRadioButtons() {
    return Array.from(this.querySelectorAll("vaadin-radio-button"));
  }
  queryRadioButtonForMode(t) {
    return this.querySelector(`vaadin-radio-button#${t}-mode-radio-btn`);
  }
};
ie.TRANSITION_STATUS_ATTR_KEY = "transition-status";
ie.PLAY_MODE_ICON_PROGRESS_CSS_VAR = "--play-mode-icon-progress";
Ve([
  dn("#mode-panel-icon-container")
], ie.prototype, "modePanelIconContainer", 2);
Ve([
  dn("#devtools-popover")
], ie.prototype, "devToolsPopover", 2);
Ve([
  dn("vaadin-radio-group")
], ie.prototype, "radioGroup", 2);
Ve([
  ve()
], ie.prototype, "modeRadioButtonItems", 2);
Ve([
  ve()
], ie.prototype, "devToolsDataLoading", 2);
Ve([
  ve()
], ie.prototype, "devToolsDataLoaded", 2);
ie = Ve([
  Le("copilot-toolbar")
], ie);
function xo() {
  const t = [];
  O.userInfo?.vaadiner && t.push({
    name: "Vaadin Employee",
    value: "true",
    booleanInfo: { ariaLabel: "Yes" }
  });
  const e = [
    ...K.serverVersions.map((r) => ({ name: r.name, value: r.version })),
    ...t,
    ...K.clientVersions.map((r) => ({ name: r.name, value: r.version }))
  ];
  return e.forEach((r) => {
    r.name === "Frontend Hotswap" && (r.value === "true" ? r.booleanInfo = { ariaLabel: "Enabled", text: "Vite" } : r.value === "false" && (r.booleanInfo = { ariaLabel: "Disabled", text: "Pre-Built Bundle" }));
  }), K.springSecurityEnabled && e.push({ name: "Spring Security", value: "true", booleanInfo: { ariaLabel: "Enabled" } }), K.springJpaDataEnabled && e.push({ name: "Spring Data JPA", value: "true", booleanInfo: { ariaLabel: "Enabled" } }), e.push(...Do()), Co(e), e;
}
function Co(t) {
  for (const e of t)
    e.value === "true" ? (e.value = !0, e.booleanInfo ? e.booleanInfo.ariaLabel || (e.booleanInfo.ariaLabel = "Enabled") : e.booleanInfo = { ariaLabel: "Enabled" }) : e.value === "false" && (e.value = !1, e.booleanInfo ? e.booleanInfo.ariaLabel || (e.booleanInfo.ariaLabel = "Disabled") : e.booleanInfo = { ariaLabel: "Disabled" });
}
function Do() {
  const t = [], e = cn(), r = Qi(O.idePluginState), n = ea(e);
  return t.push({
    name: "Java Hotswap",
    value: e === "success",
    booleanInfo: {
      ariaLabel: e === "success" ? "Enabled" : "Disabled",
      text: n
    }
  }), It() !== "unsupported" && t.push({
    name: "IDE Plugin",
    value: It() === "success",
    booleanInfo: {
      ariaLabel: It() === "success" ? "Installed" : "Not Installed",
      text: r
    }
  }), t;
}
function Oo(t) {
  const e = O.projectInfoEntries;
  if (!e)
    throw new Error("Unable to load project info entries");
  const i = e.map((a) => ({ key: a.name, value: a.value })).filter((a) => a.key !== "Live reload").filter((a) => !a.key.startsWith("Vaadin Emplo")).filter((a) => a.key !== "Development Workflow").map((a) => {
    const { key: o } = a;
    let { value: d } = a;
    if (o === "IDE Plugin")
      d = Qi(O.idePluginState) ?? "false";
    else if (o === "Java Hotswap") {
      const c = K.jdkInfo?.jrebel, s = cn();
      c && s === "success" ? d = "JRebel is in use" : d = ea(s);
    }
    return o === "Frontend Hotswap" && (d === "true" || d === !0 ? d = "Vite" : (d === "false" || d === !1) && (d = "Pre-Built Bundle")), `${o}: ${d}`;
  }).join(`
`);
  return t && Pe({
    type: se.INFORMATION,
    message: "Environment information copied to clipboard"
  }), i.trim();
}
function It() {
  return O.idePluginState !== void 0 && !O.idePluginState.active ? "warning" : "success";
}
function Qi(t) {
  if (It() !== "success")
    return "Not installed";
  if (t?.version) {
    let e = null;
    return t?.ide && (t?.ide === "intellij" ? e = "IntelliJ" : t?.ide === "vscode" ? e = "VS Code" : t?.ide === "eclipse" && (e = "Eclipse")), e ? `${t?.version} ${e}` : t?.version;
  }
  return "Not installed";
}
function ea(t) {
  return t === "success" ? "Java Hotswap is enabled" : t === "error" ? "Hotswap is partially enabled" : "Hotswap is disabled";
}
st(
  () => [
    K.serverVersions,
    K.clientVersions,
    O.userInfo,
    K.springSecurityEnabled,
    K.springJpaDataEnabled,
    K.jdkInfo,
    O.idePluginState
  ],
  () => {
    const t = xo();
    O.setProjectInfoEntries(t);
  },
  { fireImmediately: !0 }
);
X.on("system-info-with-callback", (t) => {
  t.detail.callback(Oo(t.detail.notify));
});
var Ao = Object.getOwnPropertyDescriptor, Po = (t, e, r, n) => {
  for (var i = n > 1 ? void 0 : n ? Ao(e, r) : e, a = t.length - 1, o; a >= 0; a--)
    (o = t[a]) && (i = o(i) || i);
  return i;
};
let Hn = class extends _t {
  constructor() {
    super(...arguments), this.renderDialog = () => C`
    <p class="text-xs text-secondary mb-3 mt-0">
      Sign in with your Vaadin account to access all Copilot features, including:
    </p>
    <ul class="gap-3 grid grid-cols-3 list-none m-0 pb-3 ps-0 w-4xl">
      <li class="bg-violet-3 dark:bg-violet-5 flex flex-col gap-6 pb-4 pt-6 px-5 rounded-md">
        <svg
          class="max-h-10"
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 960 641.453"
          role="img"
          artist="Katerina Limpitsouni"
          source="https://undraw.co/">
          <g transform="translate(-841.956 -428.663)">
            <rect width="596" height="596" transform="translate(1205.956 474.116)" fill="#fff" />
            <path
              d="M53.661,0H89.435V1.491H53.661Zm53.661,0H143.1V1.491H107.322Zm53.661,0h35.774V1.491H160.983Zm53.661,0h35.774V1.491H214.644ZM268.3,0h35.774V1.491H268.3Zm53.661,0H357.74V1.491H321.966Zm53.661,0H411.4V1.491H375.627Zm53.661,0h35.774V1.491H429.287Zm53.661,0h35.774V1.491H482.948Zm53.661,0h35.774V1.491H536.609ZM590.27,0h5.962V29.812h-1.491V1.491H590.27Zm4.472,47.7h1.491V83.473h-1.491Zm0,53.661h1.491v35.774h-1.491Zm0,53.661h1.491v35.774h-1.491Zm0,53.661h1.491v35.774h-1.491Zm0,53.661h1.491v35.774h-1.491Zm0,53.661h1.491v35.774h-1.491Zm0,53.661h1.491v35.774h-1.491Zm0,53.661h1.491V459.1h-1.491Zm0,53.661h1.491V512.76h-1.491Zm0,53.661h1.491v35.774h-1.491Zm0,53.661h1.491v11.925H572.383v-1.491h22.359Zm-76.02,10.434H554.5v1.491H518.722Zm-53.661,0h35.774v1.491H465.061Zm-53.661,0h35.774v1.491H411.4Zm-53.661,0h35.774v1.491H357.74Zm-53.661,0h35.774v1.491H304.079Zm-53.661,0h35.774v1.491H250.418Zm-53.661,0h35.774v1.491H196.757Zm-53.661,0H178.87v1.491H143.1Zm-53.661,0h35.774v1.491H89.435Zm-53.661,0H71.548v1.491H35.774ZM0,578.346H1.491v16.4h16.4v1.491H0Zm0-53.661H1.491v35.774H0Zm0-53.661H1.491V506.8H0Zm0-53.661H1.491v35.774H0ZM0,363.7H1.491v35.774H0Zm0-53.661H1.491v35.774H0ZM0,256.38H1.491v35.774H0Zm0-53.661H1.491v35.774H0Zm0-53.661H1.491v35.774H0ZM0,95.4H1.491v35.774H0ZM0,41.736H1.491V77.51H0ZM0,0H35.774V1.491H1.491V23.849H0Z"
              transform="translate(1205.724 473.883)"
              fill="#707070" />
            <path
              d="M39.774,4a4.472,4.472,0,0,1,4.472,4.472V35.3h26.83a4.472,4.472,0,0,1,0,8.943H44.246v26.83a4.472,4.472,0,0,1-8.943,0V44.246H8.472a4.472,4.472,0,1,1,0-8.943H35.3V8.472A4.472,4.472,0,0,1,39.774,4"
              transform="translate(1464.066 732.225)"
              fill="var(--violet-9)" />
            <path
              d="M63.7,546.533l24.915,3.661,26.98-74.678-29.37-15.885Z"
              transform="translate(784.499 495.814)"
              fill="#9f616a" />
            <path
              d="M151.118,536.837h0a21.623,21.623,0,0,1,.149,7.217h0a8.547,8.547,0,0,1-9.7,7.213L64.407,539.922a5.831,5.831,0,0,1-4.919-6.617l.473-3.212s-2.4-10.216,7.211-20.962c0,0,10.807,12.45,27.547,0l5.554-7.366,25.3,25.874,16.955,4.664c3.71,1.021,7.138.976,8.6,4.534Z"
              transform="translate(782.531 517.496)"
              fill="#090814" />
            <path
              d="M94.086,556.285H119.4l12.039-97.633H94.08Z"
              transform="translate(949.701 497.399)"
              fill="#9f616a" />
            <path
              d="M182.668,533.89h0a21.729,21.729,0,0,1,1.2,7.155h0a8.59,8.59,0,0,1-8.59,8.59H96.9a5.861,5.861,0,0,1-5.861-5.861v-3.263s-3.876-9.808,4.105-21.9c0,0,9.921,9.465,24.744-5.366l4.372-7.921L155.9,528.473l17.541,2.158c3.838.473,7.24-.073,9.215,3.251Z"
              transform="translate(947.837 519.221)"
              fill="#090814" />
            <path
              d="M16.9,90.494V65.107a35.093,35.093,0,1,1,35.245.667c1.589,14.833.341,33.245.341,33.245Z"
              transform="translate(965.657 439.744)"
              fill="#9f616a" />
            <path
              d="M409.292,442.046s-7.784,93.583-9.455,113.637a142.614,142.614,0,0,1-8.355,36.764,34.568,34.568,0,0,0-3.342,13.369s-21.4,35.947-34.86,62.255a188.838,188.838,0,0,0-17.3,50.948s33.139,6.112,38.159,4.442,11.477-38.08,67.485-129.342l40.107-83.556s23.4,75.2,28.409,83.556c0,0,8.748,123.462,12.09,131.818s45.378-2.476,45.378-2.476l-7.335-126-15.04-142.045-76.871-23.4Z"
              transform="translate(516.199 287.857)"
              fill="#090814" />
            <path
              d="M24.592,220.713s3.344-11.7,0-13.371,8.357-5.009,5.015-13.367S36.291,140.5,36.291,140.5L33.733,97.013l-2.814,4.221L0,98.73S8.358,61.97,10.028,50.269c1.339-9.382,14.488-18.759,19.655-22.113l-.076-1.293,44.1-11.7c1.626.1,2.921-3.739,4.166-7.583S80.308-.092,81.73,0c13.459.867,22.424,1.435,28.4,1.808,11.9.741,13.061,16.457,13.061,16.457l41.778,25.31-.124.534A81.393,81.393,0,0,1,186.69,85.357c5.012,26.74,0,25.085,0,25.085l-33.423,16.692-5.5-10.571-4.529,10.568c10.028,30.081,13.546,74.442,11.7,80.213l6.683,21.724a169.721,169.721,0,0,1-56.113,9.322C59.069,238.39,24.592,220.713,24.592,220.713Z"
              transform="translate(899.799 514.203)"
              fill="#e6e6e6" />
            <path
              d="M524.97,341.55s-15.04,63.5,3.342,63.5,61.831-71.858,61.831-71.858l-15.034-18.377-26.994,37.479-1.413-19.1Z"
              transform="translate(539.191 273.15)"
              fill="#9f616a" />
            <circle cx="14.632" cy="14.632" r="14.632" transform="translate(1110.694 579.842)" fill="#9f616a" />
            <path
              d="M382.97,332.549s-15.04,63.5,3.342,63.5,61.831-71.858,61.831-71.858l-15.034-18.377-14.5,20.123-12.5,17.352-1.413-19.1Z"
              transform="translate(521.368 272.02)"
              fill="#9f616a" />
            <circle cx="14.632" cy="14.632" r="14.632" transform="translate(950.873 569.712)" fill="#9f616a" />
            <path
              d="M23.849,0H350.985a23.849,23.849,0,0,1,23.849,23.849V186.995a23.849,23.849,0,0,1-23.849,23.849H23.849A23.849,23.849,0,0,1,0,186.995V23.849A23.849,23.849,0,0,1,23.849,0Z"
              transform="matrix(0.998, 0.07, -0.07, 0.998, 865.395, 576.763)"
              fill="var(--violet-9)" />
            <path
              d="M216.737,47.078l4.435,5.377,8.022-14.037s10.239.527,10.239-7.072,14.123-7.39,14.123-7.39,11.673-8.73,6.929-13.109S248.731,3.3,234.58,6.44c0,0-15.848-10.856-25.989-4.245a12.435,12.435,0,0,0-2.611,2.347s-29.128,14.662-20.786,40.2l13.847,26.323,3.142-5.954s-1.9-25.011,14.563-18.043Z"
              transform="translate(774.225 428.603)"
              fill="#090814" />
          </g>
        </svg>
        <div class="flex flex-col gap-0.5">
          <span class="font-bold">Drag and drop components</span>
          <span class="text-secondary text-xs"
            >Build layouts faster by dragging components directly onto the canvas</span
          >
        </div>
      </li>
      <li class="bg-blue-3 dark:bg-blue-5 flex flex-col gap-6 pb-4 pt-6 px-5 rounded-md">
        <svg
          class="max-h-10"
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 710.40985 647.95797"
          role="img"
          artist="Katerina Limpitsouni"
          source="https://undraw.co/">
          <path
            d="M166.1564,117.90546c8.3487-46.78787,53.04582-77.94905,99.83369-69.60035,46.78787,8.3487,77.94937,53.04588,69.60067,99.83375-6.77085,37.94529-37.45011,65.60766-73.76176,70.25683l-35.69028,105.31194-71.07457-84.47181s22.13556-19.75542,36.43036-43.93366c-20.15394-19.25054-30.60088-47.90307-25.33811-77.3967Z"
            fill="#ed9da0" />
          <path
            d="M173.09718,214.78809s-113.68733,19.16988-128.68733,42.16988-31,56-31,56c0,0-43,214,25,244l274.68733-39.16988-33.68733-100.83012,34,101,79.53386-12.73159s-47.53386-235.26841-74.53386-251.26841c-27-16-60.75109-11.70056-60.75109-11.70056,0,0-53.74763,53.40351-83.99827-27.44796l-.5633-.02135Z"
            fill="var(--blue-9)" />
          <path
            d="M234.35198,83.56597s24.05787,7.39199,34.05787-4.60801c0,0,24,1,29,14s35,19,45,8c10-11,25-31,6-49,0,0-3-19-19-23l-9,8s-118.87599-55.90628-132.93799,1.04686c0,0-21.52687-4.05801-11.29444,8.94756,0,0-11.52977,13.36898-18.84417,38.72114-11.47969,39.78928,1.95017,82.60309,33.7639,109.11453l.00004.00003s-25.56332-67.10233-3.62533-47.46622,20.93799-25.3639,20.93799-25.3639c0,0,32.88426-19.78398,25.94213-38.39199Z"
            fill="#090814" />
          <path
            d="M695.55497,121.21685l-327.01201,27.71288c-6.91943.58639-12.30538,6.25775-12.53403,13.19822l-9.64485,292.76846c-.23689,7.19081,5.12962,13.33942,12.28642,14.07702l336.65687,34.69645c8.07792.83252,15.10249-5.50529,15.10249-13.62599V134.8661c0-8.02162-6.86192-14.32661-14.85488-13.64924Z"
            fill="#d6d6e3" />
          <polygon
            points="383.74945 177.01197 375.40985 437.24241 669.40985 463.95797 676.40985 156.95797 383.74945 177.01197"
            fill="#fff" />
          <path
            d="M398.944,210.64706l255.27397-14.34123c3.36201-.18888,6.19188,2.48647,6.19188,5.85378v50.364c0,3.15452-2.49595,5.74359-5.64835,5.85908l-255.27397,9.35261c-3.32.12164-6.07768-2.53686-6.07768-5.85908v-45.37537c0-3.11029,2.42876-5.67932,5.53415-5.85378Z"
            fill="#d6d6e3" />
          <path
            d="M578.40822,226.95801c-1.05908,0-1.94238-.83105-1.99609-1.90039-.05518-1.10254.79443-2.04199,1.89746-2.09668l60-3c1.11523-.05957,2.04248.79395,2.09766,1.89746.05518,1.10254-.79443,2.04199-1.89746,2.09668l-60,3c-.03418.00195-.06787.00293-.10156.00293Z"
            fill="#090814" />
          <path
            d="M548.40822,239.95801c-1.07373,0-1.96143-.85254-1.99756-1.93359-.03662-1.10352.82861-2.02832,1.93262-2.06543l90-2.99512c1.10303-.02344,2.02881.8291,2.06543,1.93262s-.82861,2.02832-1.93262,2.06543l-90,2.99512c-.02246.00098-.04492.00098-.06787.00098Z"
            fill="#090814" />
          <path
            d="M406.55761,241.5352l-7.5306,84.49779c-.33438,3.75189,2.56743,7.00734,6.33293,7.10472l102.89288,2.66102c3.45674.0894,6.38342-2.53245,6.67325-5.97817l7.57153-90.01708c.48254-5.73687-4.20762-10.5811-9.95709-10.28418l-98.8071,5.1028c-3.78001.19522-6.83981,3.14299-7.17581,6.9131Z"
            fill="#fff" />
          <path
            d="M513.04262,233.50635c1.54932,0,2.98352.63184,4.03845,1.7793,1.0531,1.14551,1.56104,2.63086,1.43054,4.18262l-7.57153,90.01709c-.10913,1.29883-1.21423,2.31592-2.51587,2.31592l-.06799-.00098-102.89307-2.66113c-.95117-.0249-1.54932-.54004-1.82104-.84473-.27197-.30518-.71533-.9585-.63086-1.90625l7.53064-84.49805c.15808-1.77441,1.6189-3.18164,3.39783-3.27344l98.80713-5.10254c.09924-.00488.19763-.00781.29578-.00781M513.04262,229.50635c-.1665,0-.33362.00439-.50208.01318l-98.80713,5.10254c-3.78003.19531-6.83984,3.14307-7.17578,6.91309l-7.53064,84.49805c-.33435,3.75146,2.5675,7.00732,6.33301,7.10449l102.89282,2.66113c.05713.00146.1145.00244.17139.00244,3.38184,0,6.2168-2.59229,6.50183-5.98047l7.57153-90.01709c.46838-5.56934-3.93774-10.29736-9.45496-10.29736h0Z"
            fill="#090814" />
          <polygon
            points="403.40985 313.95797 436.40985 272.95797 456.40985 297.95797 481.40985 277.95797 513.40985 317.95797 511.40985 332.95797 402.09718 328.78809 403.40985 313.95797"
            fill="#090814" />
          <path
            d="M554.40985,290.94489v60.53594c0,6.73742,5.46176,12.19917,12.19917,12.19917h77.75183c6.73742,0,12.19917-5.46176,12.19917-12.19917v-61.90072c0-6.82119-5.59314-12.31701-12.41327-12.19729l-77.75183,1.36478c-6.65293.11678-11.98507,5.54334-11.98507,12.19729Z"
            fill="#d6d6e3" />
          <path
            d="M615.41115,388.95801c-.02783,0-.05566-.00098-.08398-.00195l-104-4.28711c-1.10352-.04492-1.96143-.97656-1.91602-2.08008.0459-1.10449.97705-1.9541,2.08105-1.91602l104,4.28711c1.10352.04492,1.96143.97656,1.91602,2.08008-.04443,1.07617-.93066,1.91797-1.99707,1.91797Z"
            fill="#090814" />
          <path
            d="M638.41115,411.25879c-.0332,0-.06689-.00098-.10107-.00293l-127-6.33008c-1.10303-.05469-1.95312-.99316-1.89795-2.09668.05469-1.10352.97705-1.96191,2.09717-1.89746l127,6.33008c1.10303.05469,1.95312.99316,1.89795,2.09668-.05322,1.06934-.93701,1.90039-1.99609,1.90039Z"
            fill="#090814" />
          <path
            d="M621.41164,439.95801c-.04346,0-.08691-.00098-.13086-.00391l-124-8c-1.10205-.07129-1.93799-1.02246-1.86719-2.125.07129-1.10254,1.02686-1.92969,2.125-1.86719l124,8c1.10205.07129,1.93799,1.02246,1.86719,2.125-.06836,1.05859-.94824,1.87109-1.99414,1.87109Z"
            fill="#090814" />
          <circle cx="465.90985" cy="264.45797" r="8.5" fill="#090814" />
          <ellipse cx="396.40985" cy="187.95797" rx="3" ry="4" fill="#d6d6e3" />
          <ellipse cx="410.40985" cy="186.95797" rx="3" ry="4" fill="#d6d6e3" />
          <ellipse cx="422.40985" cy="185.95797" rx="3" ry="4" fill="#d6d6e3" />
          <path
            d="M678.81251,27c1.98352,0,3.59729,1.62109,3.59729,3.61377v63.37988c0,1.88428-1.41528,3.43115-3.29199,3.59814l-85.99927,7.66309c-.11133.01025-.22168.01514-.33118.01514-1.93237,0-3.54761-1.57666-3.60071-3.51416l-1.59314-58.15137c-.05005-1.82812,1.27612-3.40674,3.08521-3.67285l87.59277-12.8916c.1803-.02686.3623-.04004.54102-.04004M678.81251,23c-.37024,0-.74512.02686-1.12378.08252l-87.59241,12.8916c-3.81445.56104-6.60693,3.88574-6.50134,7.73975l1.59314,58.15137c.11389,4.15527,3.5249,7.40479,7.59924,7.40479.22705,0,.45557-.01025.68628-.03076l85.99915-7.66309c3.927-.3501,6.93701-3.64014,6.93701-7.58252V30.61377c0-4.2627-3.474-7.61377-7.59729-7.61377h0Z"
            fill="#d6d6e3" />
          <path
            d="M606.40724,61.95801c-.99756,0-1.86084-.74512-1.98291-1.76074-.13281-1.09668.64893-2.09277,1.74561-2.22461l58-7c1.09326-.13477,2.09326.64844,2.2251,1.74609.13281,1.09668-.64893,2.09277-1.74561,2.22461l-58,7c-.08154.00977-.16211.01465-.24219.01465Z"
            fill="#d6d6e3" />
          <path
            d="M606.40724,78.90039c-1.01904,0-1.89014-.77539-1.98877-1.81055-.10449-1.09961.70215-2.07617,1.80176-2.18066l52-4.94238c1.09717-.09863,2.07617.70215,2.18066,1.80176s-.70215,2.07617-1.80176,2.18066l-52,4.94238c-.06445.00586-.12842.00879-.19189.00879Z"
            fill="#d6d6e3" />
          <path
            d="M524.49989,4c1.98352,0,3.59729,1.62109,3.59729,3.61377v63.37988c0,1.88428-1.41528,3.43115-3.29199,3.59814l-85.99927,7.66309c-.11133.01025-.22168.01514-.33118.01514-1.93237,0-3.54761-1.57666-3.60071-3.51416l-1.59314-58.15137c-.05005-1.82812,1.27612-3.40674,3.08521-3.67285l87.59277-12.8916c.1803-.02686.3623-.04004.54102-.04004M524.49989,0c-.37024,0-.74512.02686-1.12378.08252l-87.59241,12.8916c-3.81445.56104-6.60693,3.88574-6.50134,7.73975l1.59314,58.15137c.11389,4.15527,3.5249,7.40479,7.59924,7.40479.22705,0,.45557-.01025.68628-.03076l85.99915-7.66309c3.927-.3501,6.93701-3.64014,6.93701-7.58252V7.61377c0-4.2627-3.474-7.61377-7.59729-7.61377h0Z"
            fill="#d6d6e3" />
          <path
            d="M452.09462,38.95801c-.99756,0-1.86084-.74512-1.98291-1.76074-.13281-1.09668.64893-2.09277,1.74561-2.22461l58-7c1.09326-.13477,2.09326.64844,2.2251,1.74609.13281,1.09668-.64893,2.09277-1.74561,2.22461l-58,7c-.08154.00977-.16211.01465-.24219.01465Z"
            fill="#d6d6e3" />
          <path
            d="M452.09462,55.90039c-1.01904,0-1.89014-.77539-1.98877-1.81055-.10449-1.09961.70215-2.07617,1.80176-2.18066l52-4.94238c1.09717-.09863,2.07617.70215,2.18066,1.80176s-.70215,2.07617-1.80176,2.18066l-52,4.94238c-.06445.00586-.12842.00879-.19189.00879Z"
            fill="#d6d6e3" />
          <polygon
            points="164.06416 583.34819 372.40985 647.95797 554.40985 585.84979 348.40985 544.95797 164.06416 583.34819"
            fill="#d6d6e3" />
          <polygon
            points="315.40985 561.95797 352.40985 552.95797 436.57461 571.46559 398.40985 583.34819 315.40985 561.95797"
            fill="#fff" />
          <polygon
            points="402.40985 590.85298 323.40985 614.95797 378.40985 632.95797 453.40985 604.95797 402.40985 590.85298"
            fill="#fff" />
          <path
            d="M342.82368,436.24076l-167.32222,89.15785-32.17034-47.43578,174.86391-87.70758c3.64657-12.7663,13.25103-24.84968,27.28203-32.36433,24.55064-13.14867,53.27389-7.33701,64.1556,12.98075,10.88163,20.3178-.19906,47.44737-24.74977,60.59608-14.03108,7.51469-29.41198,8.81231-42.05921,4.77302Z"
            fill="#ed9da0" />
          <path
            d="M108.40985,396.95797l-8,24s19,20.28725,0,31.64363l67-8.64363s27.12401-20.00542,29.06201-3.50271c0,0,33.93799-22.49729,40.93799-8.49729,0,0-20.07249,43.40586,11.46376,65.70293l-41.96376,18.86878-29.89001,20.22884-138.60999,20.19945"
            fill="var(--blue-9)" />
          <path
            d="M464.7671,425.68457c-.35059,0-.70361-.0166-1.05908-.05078l-57.14062-5.48438c-5.97754-.57324-10.46094-5.91113-9.99463-11.89844l3.68311-47.25195c.47168-6.04199,5.78662-10.5957,11.81836-10.19922l58.62061,4.0498c3.03174.20996,5.77734,1.60742,7.72998,3.93555,1.95312,2.32812,2.85205,5.27344,2.53125,8.29492l-5.16357,48.68652c-.60059,5.66406-5.43652,9.91797-11.02539,9.91797ZM402.74855,361.19336l2.49268.19434-3.68311,47.25195c-.25635,3.28711,2.20508,6.21777,5.48682,6.5332l57.14062,5.48438c3.31152.31934,6.28467-2.11133,6.63525-5.41797l5.16357-48.68652c.17578-1.65918-.31787-3.27637-1.39014-4.55371-1.07227-1.27832-2.57959-2.0459-4.24414-2.16113l-58.62061-4.0498c-3.31592-.21973-6.22949,2.28125-6.48828,5.59961l-2.49268-.19434Z"
            fill="var(--blue-9)" />
          <polygon
            points="402.40985 412.95797 424.40985 390.95797 431.40985 400.95797 448.40985 381.95797 470.73937 412.95797 469.40985 423.95797 403.40985 418.80504 402.40985 412.95797"
            fill="var(--blue-9)" />
        </svg>
        <div class="flex flex-col gap-0.5">
          <span class="font-bold">Duplicate and copy components</span>
          <span class="text-secondary text-xs"
            >Quickly reuse elements by duplicating or copying them across your project</span
          >
        </div>
      </li>
      <li class="bg-teal-3 dark:bg-teal-5 flex flex-col gap-6 pb-4 pt-6 px-5 rounded-md">
        <svg
          class="max-h-10"
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 799.031 618.112"
          role="img"
          artist="Katerina Limpitsouni"
          source="https://undraw.co/">
          <g transform="translate(-560.484 -230.944)">
            <path
              d="M15.18,488.763c0,.872.478,1.573,1.073,1.573h535.1c.6,0,1.073-.7,1.073-1.573s-.478-1.573-1.073-1.573H16.253C15.658,487.191,15.18,487.891,15.18,488.763Z"
              transform="translate(675.195 358.72)"
              fill="#ccc" />
            <rect width="19.105" height="3.371" transform="translate(865.646 842.298)" fill="#b6b3c5" />
            <rect width="19.105" height="3.371" transform="translate(1034.779 842.861)" fill="#b6b3c5" />
            <path
              d="M352.955,370.945a27.529,27.529,0,0,1-54.321,0H229.146V521.536h193.3V370.945Z"
              transform="translate(634.205 321.322)"
              fill="#d6d6e3" />
            <rect width="193.296" height="5.242" transform="translate(863.914 830.927)" fill="#090814" />
            <path
              d="M788.255,487.17H10.776A10.788,10.788,0,0,1,0,476.394V32.688A10.788,10.788,0,0,1,10.776,21.911H788.255a10.789,10.789,0,0,1,10.776,10.776V476.394a10.789,10.789,0,0,1-10.776,10.776Z"
              transform="translate(560.484 209.033)"
              fill="#090814" />
            <rect width="760.822" height="429.297" transform="translate(578.588 248)" fill="#fff" />
            <g transform="translate(0 -41.857)">
              <g transform="translate(-588.477 33.946)">
                <path
                  d="M35.524,67.628A24.524,24.524,0,0,1,11,43.1V36.524A24.524,24.524,0,0,1,35.524,12a1.492,1.492,0,1,1,0,2.983,21.54,21.54,0,0,0-21.54,21.54V43.1a21.54,21.54,0,1,0,43.081,0V31.259a1.492,1.492,0,1,1,2.983,0V43.1A24.524,24.524,0,0,1,35.524,67.628Z"
                  transform="translate(1535.985 422.718)" />
                <path
                  d="M28.524,67.628A24.524,24.524,0,0,1,4,43.1V31.259a1.492,1.492,0,1,1,2.983,0V43.1a21.54,21.54,0,1,0,43.081,0V36.524a21.54,21.54,0,0,0-21.54-21.54,1.492,1.492,0,0,1,0-2.983A24.524,24.524,0,0,1,53.047,36.524V43.1A24.524,24.524,0,0,1,28.524,67.628Z"
                  transform="translate(1496.922 422.718)" />
                <path
                  d="M58.556,46.441a1.492,1.492,0,0,1-1.492-1.492V26.524a21.54,21.54,0,1,0-43.081,0,1.492,1.492,0,1,1-2.983,0,24.524,24.524,0,1,1,49.047,0V44.949A1.492,1.492,0,0,1,58.556,46.441Z"
                  transform="translate(1535.985 366.911)" />
                <path
                  d="M51.556,93.821a1.492,1.492,0,0,1-1.492-1.492V26.524a21.54,21.54,0,1,0-43.081,0V44.949a1.492,1.492,0,0,1-2.983,0V26.524A24.524,24.524,0,0,1,45.864,9.183a24.363,24.363,0,0,1,7.183,17.341V92.329A1.492,1.492,0,0,1,51.556,93.821Z"
                  transform="translate(1496.922 366.911)" />
                <g transform="translate(1570.017 382.073)">
                  <path
                    d="M20.782,57.047a1.492,1.492,0,1,1,0-2.983,21.54,21.54,0,1,0,0-43.081h-3.29a1.492,1.492,0,0,1,0-2.983h3.29a24.524,24.524,0,1,1,0,49.047Z"
                    transform="translate(-10.602 18.322)" />
                  <path
                    d="M19.372,37.305a1.492,1.492,0,1,1,0-2.983,11.67,11.67,0,1,0,0-23.339h-1.88a1.492,1.492,0,0,1,0-2.983h1.88a14.653,14.653,0,1,1,0,29.305Z"
                    transform="translate(-16 -8)" />
                  <path
                    d="M19.372,37.305h-1.88a1.492,1.492,0,1,1,0-2.983h1.88a11.67,11.67,0,0,0,0-23.339,1.492,1.492,0,0,1,0-2.983,14.653,14.653,0,0,1,0,29.305Z"
                    transform="translate(-16 62.894)" />
                </g>
                <g transform="translate(1492.234 382.073)">
                  <path
                    d="M40.523,57.047A24.524,24.524,0,0,1,40.523,8h3.29a1.492,1.492,0,1,1,0,2.983h-3.29a21.54,21.54,0,0,0,0,43.081,1.492,1.492,0,0,1,0,2.983Z"
                    transform="translate(-16 18.322)" />
                  <path
                    d="M30.652,37.305A14.653,14.653,0,0,1,30.652,8h1.88a1.492,1.492,0,1,1,0,2.983h-1.88a11.67,11.67,0,0,0,0,23.339,1.492,1.492,0,0,1,0,2.983Z"
                    transform="translate(0.678 -8)" />
                  <path
                    d="M32.532,37.305h-1.88A14.653,14.653,0,0,1,30.652,8a1.492,1.492,0,0,1,0,2.983,11.67,11.67,0,0,0,0,23.339h1.88a1.492,1.492,0,1,1,0,2.983Z"
                    transform="translate(0.679 62.894)" />
                </g>
              </g>
              <g transform="translate(864.012 553.398)">
                <rect width="29.619" height="7.13" rx="3.565" transform="translate(37.298)" fill="var(--teal-9)" />
                <rect width="10.421" height="7.13" rx="3.565" transform="translate(159.064)" fill="var(--teal-9)" />
                <rect width="10.421" height="7.13" rx="3.565" transform="translate(179.908)" fill="var(--teal-9)" />
                <rect width="70.756" height="7.13" rx="3.565" transform="translate(77.338)" fill="var(--teal-9)" />
                <rect
                  width="29.619"
                  height="7.13"
                  rx="3.565"
                  transform="translate(0.001 46.074)"
                  fill="var(--teal-9)" />
                <rect
                  width="10.421"
                  height="7.13"
                  rx="3.565"
                  transform="translate(121.767 46.074)"
                  fill="var(--teal-9)" />
                <rect
                  width="10.421"
                  height="7.13"
                  rx="3.565"
                  transform="translate(142.61 46.074)"
                  fill="var(--teal-9)" />
                <rect
                  width="70.756"
                  height="7.13"
                  rx="3.565"
                  transform="translate(40.041 46.074)"
                  fill="var(--teal-9)" />
                <rect
                  width="29.619"
                  height="7.13"
                  rx="3.565"
                  transform="translate(122.316 15.906)"
                  fill="var(--teal-9)" />
                <rect
                  width="29.619"
                  height="7.13"
                  rx="3.565"
                  transform="translate(0.001 15.906)"
                  fill="var(--teal-9)" />
                <rect width="10.421" height="7.13" rx="3.565" transform="translate(0.001)" fill="var(--teal-9)" />
                <rect width="10.421" height="7.13" rx="3.565" transform="translate(0 31.264)" fill="var(--teal-9)" />
                <rect
                  width="70.756"
                  height="7.13"
                  rx="3.565"
                  transform="translate(41.686 15.906)"
                  fill="var(--teal-9)" />
                <rect
                  width="29.619"
                  height="7.13"
                  rx="3.565"
                  transform="translate(60.884 31.264)"
                  fill="var(--teal-9)" />
                <rect
                  width="29.619"
                  height="7.13"
                  rx="3.565"
                  transform="translate(20.843 31.264)"
                  fill="var(--teal-9)" />
                <rect width="10.421" height="7.13" rx="3.565" transform="translate(18.675)" fill="var(--teal-9)" />
                <rect
                  width="10.421"
                  height="7.13"
                  rx="3.565"
                  transform="translate(181.553 31.264)"
                  fill="var(--teal-9)" />
                <rect
                  width="70.756"
                  height="7.13"
                  rx="3.565"
                  transform="translate(100.375 31.264)"
                  fill="var(--teal-9)" />
              </g>
            </g>
            <g transform="translate(626.555 602.469)">
              <path
                d="M805.134,330.7H727.95a1.546,1.546,0,0,1-1.544-1.544V314.612h.618V329.16a.928.928,0,0,0,.927.927h77.184a.928.928,0,0,0,.927-.927V314.51h.618V329.16A1.546,1.546,0,0,1,805.134,330.7Z"
                transform="translate(-646.44 -292.702)"
                fill="#3f3d56" />
              <rect width="181.374" height="0.618" transform="translate(5.3 21.601)" fill="#3f3d56" />
              <ellipse
                cx="5.313"
                cy="5.313"
                rx="5.313"
                ry="5.313"
                transform="translate(0.001 16.549)"
                fill="var(--teal-9)" />
              <ellipse
                cx="5.313"
                cy="5.313"
                rx="5.313"
                ry="5.313"
                transform="translate(53.991 16.549)"
                fill="var(--teal-9)" />
              <ellipse
                cx="5.313"
                cy="5.313"
                rx="5.313"
                ry="5.313"
                transform="translate(90.634 32.165)"
                fill="#3f3d56" />
              <ellipse cx="5.313" cy="5.313" rx="5.313" ry="5.313" transform="translate(118.489 32.165)" fill="#ccc" />
              <ellipse
                cx="5.313"
                cy="5.313"
                rx="5.313"
                ry="5.313"
                transform="translate(104.991 16.549)"
                fill="var(--teal-9)" />
              <ellipse
                cx="5.313"
                cy="5.313"
                rx="5.313"
                ry="5.313"
                transform="translate(180.632 16.549)"
                fill="var(--teal-9)" />
              <ellipse
                cx="5.313"
                cy="5.313"
                rx="5.313"
                ry="5.313"
                transform="translate(154.616 16.549)"
                fill="var(--teal-9)" />
              <path
                d="M537.36,277.577a.309.309,0,0,1-.309-.309V262.022a1.546,1.546,0,0,1,1.544-1.544H553.63a.309.309,0,1,1,0,.618H538.6a.928.928,0,0,0-.927.927v15.246a.309.309,0,0,1-.309.309Z"
                transform="translate(-515.571 -255.358)"
                fill="#3f3d56" />
              <ellipse cx="5.313" cy="5.313" rx="5.313" ry="5.313" transform="translate(33.452 0)" fill="#e6e6e6" />
              <path
                d="M921.669,277.268h-.618V262.022a1.546,1.546,0,0,1,1.544-1.544H937.63v.618H922.6a.928.928,0,0,0-.927.927Z"
                transform="translate(-780.967 -255.358)"
                fill="#3f3d56" />
              <ellipse cx="5.313" cy="5.313" rx="5.313" ry="5.313" transform="translate(152.058 0)" fill="#e6e6e6" />
            </g>
            <path
              d="M496.375,205.477c-2.221,0-4.027.792-4.027,1.764v1.411c0,.973,1.806,1.764,4.027,1.764h93.434c2.221,0,4.027-.792,4.027-1.764v-1.411c0-.973-1.806-1.764-4.027-1.764Z"
              transform="translate(635.637 363.33)"
              fill="#f2f2f2" />
            <path
              d="M670.026,309.282c4,0,7.249,1.75,7.249,3.9v30.351c0,2.152-3.252,3.9-7.249,3.9H497.656c-4,0-7.249-1.75-7.249-3.9V313.184c0-2.152,3.252-3.9,7.249-3.9"
              transform="translate(637.578 297.505)"
              fill="#f2f2f2" />
            <path
              d="M496.375,234.581c-2.221,0-4.027.973-4.027,2.168s1.806,2.168,4.027,2.168H639.748c2.221,0,4.027-.973,4.027-2.168s-1.806-2.168-4.027-2.168Z"
              transform="translate(635.637 343.828)"
              fill="#f2f2f2" />
            <path
              d="M496.375,234.581c-2.221,0-4.027.973-4.027,2.168s1.806,2.168,4.027,2.168H639.748c2.221,0,4.027-.973,4.027-2.168s-1.806-2.168-4.027-2.168Z"
              transform="translate(635.637 352.828)"
              fill="#f2f2f2" />
            <path
              d="M891.9,191.277H840.311a1.683,1.683,0,1,1,0-3.367H891.9a1.683,1.683,0,1,1,0,3.367Z"
              transform="translate(-212.074 93.872)"
              fill="var(--teal-9)" />
            <path
              d="M862.672,210.649H840.311a1.683,1.683,0,1,1,0-3.367h22.361a1.683,1.683,0,1,1,0,3.367Z"
              transform="translate(-212.074 81.146)"
              fill="var(--teal-9)" />
            <g transform="translate(690.275 280.103)">
              <ellipse cx="6.686" cy="6.686" rx="6.686" ry="6.686" transform="translate(0 0)" fill="var(--teal-9)" />
              <path
                d="M847.243,585.331H847.2a.874.874,0,0,1-.646-.336l-1.118-1.434a.875.875,0,0,1,.154-1.228l.04-.032a.874.874,0,0,1,1.228.154.638.638,0,0,0,.966.047l2.267-2.4a.876.876,0,0,1,1.237-.034l.037.035a.874.874,0,0,1,.034,1.237l-3.521,3.716a.874.874,0,0,1-.635.273Z"
                transform="translate(-841.667 -576.242)"
                fill="#fff" />
            </g>
            <path
              d="M891.9,191.277H840.311a1.683,1.683,0,1,1,0-3.367H891.9a1.683,1.683,0,1,1,0,3.367Z"
              transform="translate(-212.074 129.452)"
              fill="var(--teal-9)" />
            <path
              d="M862.672,210.649H840.311a1.683,1.683,0,1,1,0-3.367h22.361a1.683,1.683,0,1,1,0,3.367Z"
              transform="translate(-212.074 116.727)"
              fill="var(--teal-9)" />
            <g transform="translate(690.275 315.683)">
              <ellipse cx="6.686" cy="6.686" rx="6.686" ry="6.686" transform="translate(0 0)" fill="#e6e6e6" />
              <path
                d="M847.243,585.331H847.2a.874.874,0,0,1-.646-.336l-1.118-1.434a.875.875,0,0,1,.154-1.228l.04-.032a.874.874,0,0,1,1.228.154.638.638,0,0,0,.966.047l2.267-2.4a.876.876,0,0,1,1.237-.034l.037.035a.874.874,0,0,1,.034,1.237l-3.521,3.716a.874.874,0,0,1-.635.273Z"
                transform="translate(-841.667 -576.242)"
                fill="#fff" />
            </g>
            <path
              d="M891.9,191.277H840.311a1.683,1.683,0,1,1,0-3.367H891.9a1.683,1.683,0,1,1,0,3.367Z"
              transform="translate(-212.074 165.032)"
              fill="var(--teal-9)" />
            <path
              d="M862.672,210.649H840.311a1.683,1.683,0,1,1,0-3.367h22.361a1.683,1.683,0,1,1,0,3.367Z"
              transform="translate(-212.074 152.307)"
              fill="var(--teal-9)" />
            <g transform="translate(690.275 351.262)">
              <ellipse cx="6.686" cy="6.686" rx="6.686" ry="6.686" transform="translate(0 0)" fill="#e6e6e6" />
              <path
                d="M847.243,585.331H847.2a.874.874,0,0,1-.646-.336l-1.118-1.434a.875.875,0,0,1,.154-1.228l.04-.032a.874.874,0,0,1,1.228.154.638.638,0,0,0,.966.047l2.267-2.4a.876.876,0,0,1,1.237-.034l.037.035a.874.874,0,0,1,.034,1.237l-3.521,3.716a.874.874,0,0,1-.635.273Z"
                transform="translate(-841.667 -576.242)"
                fill="#fff" />
            </g>
            <path
              d="M891.9,191.277H840.311a1.683,1.683,0,1,1,0-3.367H891.9a1.683,1.683,0,1,1,0,3.367Z"
              transform="translate(-212.074 200.611)"
              fill="#e6e6e6" />
            <path
              d="M862.672,210.649H840.311a1.683,1.683,0,1,1,0-3.367h22.361a1.683,1.683,0,1,1,0,3.367Z"
              transform="translate(-212.074 187.886)"
              fill="#e6e6e6" />
            <g transform="translate(690.275 386.842)">
              <ellipse cx="6.686" cy="6.686" rx="6.686" ry="6.686" transform="translate(0 0)" fill="#e6e6e6" />
              <path
                d="M847.243,585.331H847.2a.874.874,0,0,1-.646-.336l-1.118-1.434a.875.875,0,0,1,.154-1.228l.04-.032a.874.874,0,0,1,1.228.154.638.638,0,0,0,.966.047l2.267-2.4a.876.876,0,0,1,1.237-.034l.037.035a.874.874,0,0,1,.034,1.237l-3.521,3.716a.874.874,0,0,1-.635.273Z"
                transform="translate(-841.667 -576.242)"
                fill="#fff" />
            </g>
            <path
              d="M891.9,191.277H840.311a1.683,1.683,0,1,1,0-3.367H891.9a1.683,1.683,0,1,1,0,3.367Z"
              transform="translate(-212.074 236.191)"
              fill="#e6e6e6" />
            <path
              d="M862.672,210.649H840.311a1.683,1.683,0,1,1,0-3.367h22.361a1.683,1.683,0,1,1,0,3.367Z"
              transform="translate(-212.074 223.466)"
              fill="#e6e6e6" />
            <g transform="translate(690.275 422.422)">
              <ellipse cx="6.686" cy="6.686" rx="6.686" ry="6.686" transform="translate(0 0)" fill="#e6e6e6" />
              <path
                d="M847.243,585.331H847.2a.874.874,0,0,1-.646-.336l-1.118-1.434a.875.875,0,0,1,.154-1.228l.04-.032a.874.874,0,0,1,1.228.154.638.638,0,0,0,.966.047l2.267-2.4a.876.876,0,0,1,1.237-.034l.037.035a.874.874,0,0,1,.034,1.237l-3.521,3.716a.874.874,0,0,1-.635.273Z"
                transform="translate(-841.667 -576.242)"
                fill="#fff" />
            </g>
            <g transform="translate(587.66 -327.248)">
              <path
                d="M345.8,318H248.438a.3.3,0,0,1-.3-.3c0-2.109,97.967-.168,97.967,0A.3.3,0,0,1,345.8,318Z"
                transform="translate(381.092 338.302)"
                fill="#090814" />
              <path
                d="M290.014,369.407h-8.855a.905.905,0,0,1-.9-.9V356.3a.905.905,0,0,1,.9-.9h8.855a.905.905,0,0,1,.9.9V368.5A.905.905,0,0,1,290.014,369.407Z"
                transform="translate(360.316 283.544)"
                fill="var(--teal-9)" />
              <path
                d="M335.73,348.208h-8.855a.905.905,0,0,1-.9-.9V323.518a.905.905,0,0,1,.9-.9h8.855a.905.905,0,0,1,.9.9V347.3A.905.905,0,0,1,335.73,348.208Z"
                transform="translate(330.75 304.743)"
                fill="var(--teal-9)" />
              <path
                d="M381.445,369.407H372.59a.905.905,0,0,1-.9-.9V356.3a.905.905,0,0,1,.9-.9h8.855a.905.905,0,0,1,.9.9V368.5A.905.905,0,0,1,381.445,369.407Z"
                transform="translate(301.181 283.544)"
                fill="var(--teal-9)" />
              <path
                d="M427.161,339.839h-8.855a.886.886,0,0,1-.9-.863V310.539a.886.886,0,0,1,.9-.863h8.855a.886.886,0,0,1,.9.863v28.437A.886.886,0,0,1,427.161,339.839Z"
                transform="translate(271.615 313.112)"
                fill="var(--teal-9)" />
              <path
                d="M472.877,324.777h-8.855a.905.905,0,0,1-.9-.9V287.291a.905.905,0,0,1,.9-.9h8.855a.905.905,0,0,1,.9.9v36.581A.905.905,0,0,1,472.877,324.777Z"
                transform="translate(242.049 328.175)"
                fill="var(--teal-9)" />
            </g>
          </g>
        </svg>
        <div class="flex flex-col gap-0.5">
          <span class="font-bold">Built-in AI tools</span>
          <span class="text-secondary text-xs">Generate views from images, use the docs assistant, and more</span>
        </div>
      </li>
    </ul>
  `, this.renderFooter = () => C`
    <vaadin-button @click=${this.cancelClickListener}>Cancel</vaadin-button>
    <vaadin-button theme="primary" @click=${this.loginClickListener}>Login</vaadin-button>
  `, this.tryAcquireLicense = () => {
      const t = "vaadin-copilot", e = K.serverVersions.find(
        (r) => r.name === "Vaadin"
      )?.version;
      window.Vaadin.devTools.downloadLicense({ name: t, version: e });
    };
  }
  createRenderRoot() {
    return this;
  }
  render() {
    return C` <vaadin-dialog
      @opened-changed="${(t) => {
      const e = t.detail.value;
      O.setLoginCheckActive(e);
    }}"
      .opened="${O.loginCheckActive}"
      ${kt(
      () => C`
          <h2 class="font-bold me-auto my-0 text-xs truncate uppercase">Unlock all AI features</h2>
          <vaadin-button aria-label="Close" @click="${() => {
      }}" theme="icon tertiary">
            <vaadin-icon .svg="${ee.close}"></vaadin-icon>
            <vaadin-tooltip slot="tooltip" text="Close"></vaadin-tooltip>
          </vaadin-button>
        `
    )}
      ${Ht(this.renderDialog, [])}
      ${Ft(this.renderFooter, [])}>
    </vaadin-dialog>`;
  }
  cancelClickListener(t) {
    O.setLoginCheckActive(!1);
  }
  loginClickListener() {
    ln("license-acquired"), this.tryAcquireLicense();
  }
};
Hn = Po([
  Le("copilot-login-check")
], Hn);
var vt = { exports: {} }, ar, kn;
function qt() {
  if (kn) return ar;
  kn = 1;
  const t = "2.0.0", e = 256, r = Number.MAX_SAFE_INTEGER || /* istanbul ignore next */
  9007199254740991, n = 16, i = e - 6;
  return ar = {
    MAX_LENGTH: e,
    MAX_SAFE_COMPONENT_LENGTH: n,
    MAX_SAFE_BUILD_LENGTH: i,
    MAX_SAFE_INTEGER: r,
    RELEASE_TYPES: [
      "major",
      "premajor",
      "minor",
      "preminor",
      "patch",
      "prepatch",
      "prerelease"
    ],
    SEMVER_SPEC_VERSION: t,
    FLAG_INCLUDE_PRERELEASE: 1,
    FLAG_LOOSE: 2
  }, ar;
}
var or, Fn;
function Bt() {
  return Fn || (Fn = 1, or = typeof process == "object" && process.env && process.env.NODE_DEBUG && /\bsemver\b/i.test(process.env.NODE_DEBUG) ? (...e) => console.error("SEMVER", ...e) : () => {
  }), or;
}
var Vn;
function ut() {
  return Vn || (Vn = 1, function(t, e) {
    const {
      MAX_SAFE_COMPONENT_LENGTH: r,
      MAX_SAFE_BUILD_LENGTH: n,
      MAX_LENGTH: i
    } = qt(), a = Bt();
    e = t.exports = {};
    const o = e.re = [], d = e.safeRe = [], c = e.src = [], s = e.safeSrc = [], l = e.t = {};
    let u = 0;
    const h = "[a-zA-Z0-9-]", p = [
      ["\\s", 1],
      ["\\d", i],
      [h, n]
    ], b = (I) => {
      for (const [_, k] of p)
        I = I.split(`${_}*`).join(`${_}{0,${k}}`).split(`${_}+`).join(`${_}{1,${k}}`);
      return I;
    }, f = (I, _, k) => {
      const A = b(_), V = u++;
      a(I, V, _), l[I] = V, c[V] = _, s[V] = A, o[V] = new RegExp(_, k ? "g" : void 0), d[V] = new RegExp(A, k ? "g" : void 0);
    };
    f("NUMERICIDENTIFIER", "0|[1-9]\\d*"), f("NUMERICIDENTIFIERLOOSE", "\\d+"), f("NONNUMERICIDENTIFIER", `\\d*[a-zA-Z-]${h}*`), f("MAINVERSION", `(${c[l.NUMERICIDENTIFIER]})\\.(${c[l.NUMERICIDENTIFIER]})\\.(${c[l.NUMERICIDENTIFIER]})`), f("MAINVERSIONLOOSE", `(${c[l.NUMERICIDENTIFIERLOOSE]})\\.(${c[l.NUMERICIDENTIFIERLOOSE]})\\.(${c[l.NUMERICIDENTIFIERLOOSE]})`), f("PRERELEASEIDENTIFIER", `(?:${c[l.NONNUMERICIDENTIFIER]}|${c[l.NUMERICIDENTIFIER]})`), f("PRERELEASEIDENTIFIERLOOSE", `(?:${c[l.NONNUMERICIDENTIFIER]}|${c[l.NUMERICIDENTIFIERLOOSE]})`), f("PRERELEASE", `(?:-(${c[l.PRERELEASEIDENTIFIER]}(?:\\.${c[l.PRERELEASEIDENTIFIER]})*))`), f("PRERELEASELOOSE", `(?:-?(${c[l.PRERELEASEIDENTIFIERLOOSE]}(?:\\.${c[l.PRERELEASEIDENTIFIERLOOSE]})*))`), f("BUILDIDENTIFIER", `${h}+`), f("BUILD", `(?:\\+(${c[l.BUILDIDENTIFIER]}(?:\\.${c[l.BUILDIDENTIFIER]})*))`), f("FULLPLAIN", `v?${c[l.MAINVERSION]}${c[l.PRERELEASE]}?${c[l.BUILD]}?`), f("FULL", `^${c[l.FULLPLAIN]}$`), f("LOOSEPLAIN", `[v=\\s]*${c[l.MAINVERSIONLOOSE]}${c[l.PRERELEASELOOSE]}?${c[l.BUILD]}?`), f("LOOSE", `^${c[l.LOOSEPLAIN]}$`), f("GTLT", "((?:<|>)?=?)"), f("XRANGEIDENTIFIERLOOSE", `${c[l.NUMERICIDENTIFIERLOOSE]}|x|X|\\*`), f("XRANGEIDENTIFIER", `${c[l.NUMERICIDENTIFIER]}|x|X|\\*`), f("XRANGEPLAIN", `[v=\\s]*(${c[l.XRANGEIDENTIFIER]})(?:\\.(${c[l.XRANGEIDENTIFIER]})(?:\\.(${c[l.XRANGEIDENTIFIER]})(?:${c[l.PRERELEASE]})?${c[l.BUILD]}?)?)?`), f("XRANGEPLAINLOOSE", `[v=\\s]*(${c[l.XRANGEIDENTIFIERLOOSE]})(?:\\.(${c[l.XRANGEIDENTIFIERLOOSE]})(?:\\.(${c[l.XRANGEIDENTIFIERLOOSE]})(?:${c[l.PRERELEASELOOSE]})?${c[l.BUILD]}?)?)?`), f("XRANGE", `^${c[l.GTLT]}\\s*${c[l.XRANGEPLAIN]}$`), f("XRANGELOOSE", `^${c[l.GTLT]}\\s*${c[l.XRANGEPLAINLOOSE]}$`), f("COERCEPLAIN", `(^|[^\\d])(\\d{1,${r}})(?:\\.(\\d{1,${r}}))?(?:\\.(\\d{1,${r}}))?`), f("COERCE", `${c[l.COERCEPLAIN]}(?:$|[^\\d])`), f("COERCEFULL", c[l.COERCEPLAIN] + `(?:${c[l.PRERELEASE]})?(?:${c[l.BUILD]})?(?:$|[^\\d])`), f("COERCERTL", c[l.COERCE], !0), f("COERCERTLFULL", c[l.COERCEFULL], !0), f("LONETILDE", "(?:~>?)"), f("TILDETRIM", `(\\s*)${c[l.LONETILDE]}\\s+`, !0), e.tildeTrimReplace = "$1~", f("TILDE", `^${c[l.LONETILDE]}${c[l.XRANGEPLAIN]}$`), f("TILDELOOSE", `^${c[l.LONETILDE]}${c[l.XRANGEPLAINLOOSE]}$`), f("LONECARET", "(?:\\^)"), f("CARETTRIM", `(\\s*)${c[l.LONECARET]}\\s+`, !0), e.caretTrimReplace = "$1^", f("CARET", `^${c[l.LONECARET]}${c[l.XRANGEPLAIN]}$`), f("CARETLOOSE", `^${c[l.LONECARET]}${c[l.XRANGEPLAINLOOSE]}$`), f("COMPARATORLOOSE", `^${c[l.GTLT]}\\s*(${c[l.LOOSEPLAIN]})$|^$`), f("COMPARATOR", `^${c[l.GTLT]}\\s*(${c[l.FULLPLAIN]})$|^$`), f("COMPARATORTRIM", `(\\s*)${c[l.GTLT]}\\s*(${c[l.LOOSEPLAIN]}|${c[l.XRANGEPLAIN]})`, !0), e.comparatorTrimReplace = "$1$2$3", f("HYPHENRANGE", `^\\s*(${c[l.XRANGEPLAIN]})\\s+-\\s+(${c[l.XRANGEPLAIN]})\\s*$`), f("HYPHENRANGELOOSE", `^\\s*(${c[l.XRANGEPLAINLOOSE]})\\s+-\\s+(${c[l.XRANGEPLAINLOOSE]})\\s*$`), f("STAR", "(<|>)?=?\\s*\\*"), f("GTE0", "^\\s*>=\\s*0\\.0\\.0\\s*$"), f("GTE0PRE", "^\\s*>=\\s*0\\.0\\.0-0\\s*$");
  }(vt, vt.exports)), vt.exports;
}
var sr, qn;
function vn() {
  if (qn) return sr;
  qn = 1;
  const t = Object.freeze({ loose: !0 }), e = Object.freeze({});
  return sr = (n) => n ? typeof n != "object" ? t : n : e, sr;
}
var lr, Bn;
function ta() {
  if (Bn) return lr;
  Bn = 1;
  const t = /^[0-9]+$/, e = (n, i) => {
    if (typeof n == "number" && typeof i == "number")
      return n === i ? 0 : n < i ? -1 : 1;
    const a = t.test(n), o = t.test(i);
    return a && o && (n = +n, i = +i), n === i ? 0 : a && !o ? -1 : o && !a ? 1 : n < i ? -1 : 1;
  };
  return lr = {
    compareIdentifiers: e,
    rcompareIdentifiers: (n, i) => e(i, n)
  }, lr;
}
var cr, jn;
function ae() {
  if (jn) return cr;
  jn = 1;
  const t = Bt(), { MAX_LENGTH: e, MAX_SAFE_INTEGER: r } = qt(), { safeRe: n, t: i } = ut(), a = vn(), { compareIdentifiers: o } = ta();
  class d {
    constructor(s, l) {
      if (l = a(l), s instanceof d) {
        if (s.loose === !!l.loose && s.includePrerelease === !!l.includePrerelease)
          return s;
        s = s.version;
      } else if (typeof s != "string")
        throw new TypeError(`Invalid version. Must be a string. Got type "${typeof s}".`);
      if (s.length > e)
        throw new TypeError(
          `version is longer than ${e} characters`
        );
      t("SemVer", s, l), this.options = l, this.loose = !!l.loose, this.includePrerelease = !!l.includePrerelease;
      const u = s.trim().match(l.loose ? n[i.LOOSE] : n[i.FULL]);
      if (!u)
        throw new TypeError(`Invalid Version: ${s}`);
      if (this.raw = s, this.major = +u[1], this.minor = +u[2], this.patch = +u[3], this.major > r || this.major < 0)
        throw new TypeError("Invalid major version");
      if (this.minor > r || this.minor < 0)
        throw new TypeError("Invalid minor version");
      if (this.patch > r || this.patch < 0)
        throw new TypeError("Invalid patch version");
      u[4] ? this.prerelease = u[4].split(".").map((h) => {
        if (/^[0-9]+$/.test(h)) {
          const p = +h;
          if (p >= 0 && p < r)
            return p;
        }
        return h;
      }) : this.prerelease = [], this.build = u[5] ? u[5].split(".") : [], this.format();
    }
    format() {
      return this.version = `${this.major}.${this.minor}.${this.patch}`, this.prerelease.length && (this.version += `-${this.prerelease.join(".")}`), this.version;
    }
    toString() {
      return this.version;
    }
    compare(s) {
      if (t("SemVer.compare", this.version, this.options, s), !(s instanceof d)) {
        if (typeof s == "string" && s === this.version)
          return 0;
        s = new d(s, this.options);
      }
      return s.version === this.version ? 0 : this.compareMain(s) || this.comparePre(s);
    }
    compareMain(s) {
      return s instanceof d || (s = new d(s, this.options)), this.major < s.major ? -1 : this.major > s.major ? 1 : this.minor < s.minor ? -1 : this.minor > s.minor ? 1 : this.patch < s.patch ? -1 : this.patch > s.patch ? 1 : 0;
    }
    comparePre(s) {
      if (s instanceof d || (s = new d(s, this.options)), this.prerelease.length && !s.prerelease.length)
        return -1;
      if (!this.prerelease.length && s.prerelease.length)
        return 1;
      if (!this.prerelease.length && !s.prerelease.length)
        return 0;
      let l = 0;
      do {
        const u = this.prerelease[l], h = s.prerelease[l];
        if (t("prerelease compare", l, u, h), u === void 0 && h === void 0)
          return 0;
        if (h === void 0)
          return 1;
        if (u === void 0)
          return -1;
        if (u === h)
          continue;
        return o(u, h);
      } while (++l);
    }
    compareBuild(s) {
      s instanceof d || (s = new d(s, this.options));
      let l = 0;
      do {
        const u = this.build[l], h = s.build[l];
        if (t("build compare", l, u, h), u === void 0 && h === void 0)
          return 0;
        if (h === void 0)
          return 1;
        if (u === void 0)
          return -1;
        if (u === h)
          continue;
        return o(u, h);
      } while (++l);
    }
    // preminor will bump the version up to the next minor release, and immediately
    // down to pre-release. premajor and prepatch work the same way.
    inc(s, l, u) {
      if (s.startsWith("pre")) {
        if (!l && u === !1)
          throw new Error("invalid increment argument: identifier is empty");
        if (l) {
          const h = `-${l}`.match(this.options.loose ? n[i.PRERELEASELOOSE] : n[i.PRERELEASE]);
          if (!h || h[1] !== l)
            throw new Error(`invalid identifier: ${l}`);
        }
      }
      switch (s) {
        case "premajor":
          this.prerelease.length = 0, this.patch = 0, this.minor = 0, this.major++, this.inc("pre", l, u);
          break;
        case "preminor":
          this.prerelease.length = 0, this.patch = 0, this.minor++, this.inc("pre", l, u);
          break;
        case "prepatch":
          this.prerelease.length = 0, this.inc("patch", l, u), this.inc("pre", l, u);
          break;
        // If the input is a non-prerelease version, this acts the same as
        // prepatch.
        case "prerelease":
          this.prerelease.length === 0 && this.inc("patch", l, u), this.inc("pre", l, u);
          break;
        case "release":
          if (this.prerelease.length === 0)
            throw new Error(`version ${this.raw} is not a prerelease`);
          this.prerelease.length = 0;
          break;
        case "major":
          (this.minor !== 0 || this.patch !== 0 || this.prerelease.length === 0) && this.major++, this.minor = 0, this.patch = 0, this.prerelease = [];
          break;
        case "minor":
          (this.patch !== 0 || this.prerelease.length === 0) && this.minor++, this.patch = 0, this.prerelease = [];
          break;
        case "patch":
          this.prerelease.length === 0 && this.patch++, this.prerelease = [];
          break;
        // This probably shouldn't be used publicly.
        // 1.0.0 'pre' would become 1.0.0-0 which is the wrong direction.
        case "pre": {
          const h = Number(u) ? 1 : 0;
          if (this.prerelease.length === 0)
            this.prerelease = [h];
          else {
            let p = this.prerelease.length;
            for (; --p >= 0; )
              typeof this.prerelease[p] == "number" && (this.prerelease[p]++, p = -2);
            if (p === -1) {
              if (l === this.prerelease.join(".") && u === !1)
                throw new Error("invalid increment argument: identifier already exists");
              this.prerelease.push(h);
            }
          }
          if (l) {
            let p = [l, h];
            u === !1 && (p = [l]), o(this.prerelease[0], l) === 0 ? isNaN(this.prerelease[1]) && (this.prerelease = p) : this.prerelease = p;
          }
          break;
        }
        default:
          throw new Error(`invalid increment argument: ${s}`);
      }
      return this.raw = this.format(), this.build.length && (this.raw += `+${this.build.join(".")}`), this;
    }
  }
  return cr = d, cr;
}
var dr, Zn;
function Ye() {
  if (Zn) return dr;
  Zn = 1;
  const t = ae();
  return dr = (r, n, i = !1) => {
    if (r instanceof t)
      return r;
    try {
      return new t(r, n);
    } catch (a) {
      if (!i)
        return null;
      throw a;
    }
  }, dr;
}
var ur, Gn;
function _o() {
  if (Gn) return ur;
  Gn = 1;
  const t = Ye();
  return ur = (r, n) => {
    const i = t(r, n);
    return i ? i.version : null;
  }, ur;
}
var hr, Un;
function Lo() {
  if (Un) return hr;
  Un = 1;
  const t = Ye();
  return hr = (r, n) => {
    const i = t(r.trim().replace(/^[=v]+/, ""), n);
    return i ? i.version : null;
  }, hr;
}
var fr, Xn;
function Mo() {
  if (Xn) return fr;
  Xn = 1;
  const t = ae();
  return fr = (r, n, i, a, o) => {
    typeof i == "string" && (o = a, a = i, i = void 0);
    try {
      return new t(
        r instanceof t ? r.version : r,
        i
      ).inc(n, a, o).version;
    } catch {
      return null;
    }
  }, fr;
}
var pr, Wn;
function No() {
  if (Wn) return pr;
  Wn = 1;
  const t = Ye();
  return pr = (r, n) => {
    const i = t(r, null, !0), a = t(n, null, !0), o = i.compare(a);
    if (o === 0)
      return null;
    const d = o > 0, c = d ? i : a, s = d ? a : i, l = !!c.prerelease.length;
    if (!!s.prerelease.length && !l) {
      if (!s.patch && !s.minor)
        return "major";
      if (s.compareMain(c) === 0)
        return s.minor && !s.patch ? "minor" : "patch";
    }
    const h = l ? "pre" : "";
    return i.major !== a.major ? h + "major" : i.minor !== a.minor ? h + "minor" : i.patch !== a.patch ? h + "patch" : "prerelease";
  }, pr;
}
var mr, Yn;
function Ho() {
  if (Yn) return mr;
  Yn = 1;
  const t = ae();
  return mr = (r, n) => new t(r, n).major, mr;
}
var gr, zn;
function ko() {
  if (zn) return gr;
  zn = 1;
  const t = ae();
  return gr = (r, n) => new t(r, n).minor, gr;
}
var vr, Jn;
function Fo() {
  if (Jn) return vr;
  Jn = 1;
  const t = ae();
  return vr = (r, n) => new t(r, n).patch, vr;
}
var br, Kn;
function Vo() {
  if (Kn) return br;
  Kn = 1;
  const t = Ye();
  return br = (r, n) => {
    const i = t(r, n);
    return i && i.prerelease.length ? i.prerelease : null;
  }, br;
}
var Er, Qn;
function be() {
  if (Qn) return Er;
  Qn = 1;
  const t = ae();
  return Er = (r, n, i) => new t(r, i).compare(new t(n, i)), Er;
}
var yr, ei;
function qo() {
  if (ei) return yr;
  ei = 1;
  const t = be();
  return yr = (r, n, i) => t(n, r, i), yr;
}
var wr, ti;
function Bo() {
  if (ti) return wr;
  ti = 1;
  const t = be();
  return wr = (r, n) => t(r, n, !0), wr;
}
var Rr, ri;
function bn() {
  if (ri) return Rr;
  ri = 1;
  const t = ae();
  return Rr = (r, n, i) => {
    const a = new t(r, i), o = new t(n, i);
    return a.compare(o) || a.compareBuild(o);
  }, Rr;
}
var Ir, ni;
function jo() {
  if (ni) return Ir;
  ni = 1;
  const t = bn();
  return Ir = (r, n) => r.sort((i, a) => t(i, a, n)), Ir;
}
var Sr, ii;
function Zo() {
  if (ii) return Sr;
  ii = 1;
  const t = bn();
  return Sr = (r, n) => r.sort((i, a) => t(a, i, n)), Sr;
}
var $r, ai;
function jt() {
  if (ai) return $r;
  ai = 1;
  const t = be();
  return $r = (r, n, i) => t(r, n, i) > 0, $r;
}
var Tr, oi;
function En() {
  if (oi) return Tr;
  oi = 1;
  const t = be();
  return Tr = (r, n, i) => t(r, n, i) < 0, Tr;
}
var xr, si;
function ra() {
  if (si) return xr;
  si = 1;
  const t = be();
  return xr = (r, n, i) => t(r, n, i) === 0, xr;
}
var Cr, li;
function na() {
  if (li) return Cr;
  li = 1;
  const t = be();
  return Cr = (r, n, i) => t(r, n, i) !== 0, Cr;
}
var Dr, ci;
function yn() {
  if (ci) return Dr;
  ci = 1;
  const t = be();
  return Dr = (r, n, i) => t(r, n, i) >= 0, Dr;
}
var Or, di;
function wn() {
  if (di) return Or;
  di = 1;
  const t = be();
  return Or = (r, n, i) => t(r, n, i) <= 0, Or;
}
var Ar, ui;
function ia() {
  if (ui) return Ar;
  ui = 1;
  const t = ra(), e = na(), r = jt(), n = yn(), i = En(), a = wn();
  return Ar = (d, c, s, l) => {
    switch (c) {
      case "===":
        return typeof d == "object" && (d = d.version), typeof s == "object" && (s = s.version), d === s;
      case "!==":
        return typeof d == "object" && (d = d.version), typeof s == "object" && (s = s.version), d !== s;
      case "":
      case "=":
      case "==":
        return t(d, s, l);
      case "!=":
        return e(d, s, l);
      case ">":
        return r(d, s, l);
      case ">=":
        return n(d, s, l);
      case "<":
        return i(d, s, l);
      case "<=":
        return a(d, s, l);
      default:
        throw new TypeError(`Invalid operator: ${c}`);
    }
  }, Ar;
}
var Pr, hi;
function Go() {
  if (hi) return Pr;
  hi = 1;
  const t = ae(), e = Ye(), { safeRe: r, t: n } = ut();
  return Pr = (a, o) => {
    if (a instanceof t)
      return a;
    if (typeof a == "number" && (a = String(a)), typeof a != "string")
      return null;
    o = o || {};
    let d = null;
    if (!o.rtl)
      d = a.match(o.includePrerelease ? r[n.COERCEFULL] : r[n.COERCE]);
    else {
      const p = o.includePrerelease ? r[n.COERCERTLFULL] : r[n.COERCERTL];
      let b;
      for (; (b = p.exec(a)) && (!d || d.index + d[0].length !== a.length); )
        (!d || b.index + b[0].length !== d.index + d[0].length) && (d = b), p.lastIndex = b.index + b[1].length + b[2].length;
      p.lastIndex = -1;
    }
    if (d === null)
      return null;
    const c = d[2], s = d[3] || "0", l = d[4] || "0", u = o.includePrerelease && d[5] ? `-${d[5]}` : "", h = o.includePrerelease && d[6] ? `+${d[6]}` : "";
    return e(`${c}.${s}.${l}${u}${h}`, o);
  }, Pr;
}
var _r, fi;
function Uo() {
  if (fi) return _r;
  fi = 1;
  class t {
    constructor() {
      this.max = 1e3, this.map = /* @__PURE__ */ new Map();
    }
    get(r) {
      const n = this.map.get(r);
      if (n !== void 0)
        return this.map.delete(r), this.map.set(r, n), n;
    }
    delete(r) {
      return this.map.delete(r);
    }
    set(r, n) {
      if (!this.delete(r) && n !== void 0) {
        if (this.map.size >= this.max) {
          const a = this.map.keys().next().value;
          this.delete(a);
        }
        this.map.set(r, n);
      }
      return this;
    }
  }
  return _r = t, _r;
}
var Lr, pi;
function Ee() {
  if (pi) return Lr;
  pi = 1;
  const t = /\s+/g;
  class e {
    constructor(m, y) {
      if (y = i(y), m instanceof e)
        return m.loose === !!y.loose && m.includePrerelease === !!y.includePrerelease ? m : new e(m.raw, y);
      if (m instanceof a)
        return this.raw = m.value, this.set = [[m]], this.formatted = void 0, this;
      if (this.options = y, this.loose = !!y.loose, this.includePrerelease = !!y.includePrerelease, this.raw = m.trim().replace(t, " "), this.set = this.raw.split("||").map((E) => this.parseRange(E.trim())).filter((E) => E.length), !this.set.length)
        throw new TypeError(`Invalid SemVer Range: ${this.raw}`);
      if (this.set.length > 1) {
        const E = this.set[0];
        if (this.set = this.set.filter((R) => !f(R[0])), this.set.length === 0)
          this.set = [E];
        else if (this.set.length > 1) {
          for (const R of this.set)
            if (R.length === 1 && I(R[0])) {
              this.set = [R];
              break;
            }
        }
      }
      this.formatted = void 0;
    }
    get range() {
      if (this.formatted === void 0) {
        this.formatted = "";
        for (let m = 0; m < this.set.length; m++) {
          m > 0 && (this.formatted += "||");
          const y = this.set[m];
          for (let E = 0; E < y.length; E++)
            E > 0 && (this.formatted += " "), this.formatted += y[E].toString().trim();
        }
      }
      return this.formatted;
    }
    format() {
      return this.range;
    }
    toString() {
      return this.range;
    }
    parseRange(m) {
      const E = ((this.options.includePrerelease && p) | (this.options.loose && b)) + ":" + m, R = n.get(E);
      if (R)
        return R;
      const w = this.options.loose, D = w ? c[s.HYPHENRANGELOOSE] : c[s.HYPHENRANGE];
      m = m.replace(D, Ie(this.options.includePrerelease)), o("hyphen replace", m), m = m.replace(c[s.COMPARATORTRIM], l), o("comparator trim", m), m = m.replace(c[s.TILDETRIM], u), o("tilde trim", m), m = m.replace(c[s.CARETTRIM], h), o("caret trim", m);
      let F = m.split(" ").map((W) => k(W, this.options)).join(" ").split(/\s+/).map((W) => he(W, this.options));
      w && (F = F.filter((W) => (o("loose invalid filter", W, this.options), !!W.match(c[s.COMPARATORLOOSE])))), o("range list", F);
      const P = /* @__PURE__ */ new Map(), B = F.map((W) => new a(W, this.options));
      for (const W of B) {
        if (f(W))
          return [W];
        P.set(W.value, W);
      }
      P.size > 1 && P.has("") && P.delete("");
      const re = [...P.values()];
      return n.set(E, re), re;
    }
    intersects(m, y) {
      if (!(m instanceof e))
        throw new TypeError("a Range is required");
      return this.set.some((E) => _(E, y) && m.set.some((R) => _(R, y) && E.every((w) => R.every((D) => w.intersects(D, y)))));
    }
    // if ANY of the sets match ALL of its comparators, then pass
    test(m) {
      if (!m)
        return !1;
      if (typeof m == "string")
        try {
          m = new d(m, this.options);
        } catch {
          return !1;
        }
      for (let y = 0; y < this.set.length; y++)
        if (ce(this.set[y], m, this.options))
          return !0;
      return !1;
    }
  }
  Lr = e;
  const r = Uo(), n = new r(), i = vn(), a = Zt(), o = Bt(), d = ae(), {
    safeRe: c,
    t: s,
    comparatorTrimReplace: l,
    tildeTrimReplace: u,
    caretTrimReplace: h
  } = ut(), { FLAG_INCLUDE_PRERELEASE: p, FLAG_LOOSE: b } = qt(), f = (v) => v.value === "<0.0.0-0", I = (v) => v.value === "", _ = (v, m) => {
    let y = !0;
    const E = v.slice();
    let R = E.pop();
    for (; y && E.length; )
      y = E.every((w) => R.intersects(w, m)), R = E.pop();
    return y;
  }, k = (v, m) => (v = v.replace(c[s.BUILD], ""), o("comp", v, m), v = N(v, m), o("caret", v), v = V(v, m), o("tildes", v), v = T(v, m), o("xrange", v), v = Re(v, m), o("stars", v), v), A = (v) => !v || v.toLowerCase() === "x" || v === "*", V = (v, m) => v.trim().split(/\s+/).map((y) => G(y, m)).join(" "), G = (v, m) => {
    const y = m.loose ? c[s.TILDELOOSE] : c[s.TILDE];
    return v.replace(y, (E, R, w, D, F) => {
      o("tilde", v, E, R, w, D, F);
      let P;
      return A(R) ? P = "" : A(w) ? P = `>=${R}.0.0 <${+R + 1}.0.0-0` : A(D) ? P = `>=${R}.${w}.0 <${R}.${+w + 1}.0-0` : F ? (o("replaceTilde pr", F), P = `>=${R}.${w}.${D}-${F} <${R}.${+w + 1}.0-0`) : P = `>=${R}.${w}.${D} <${R}.${+w + 1}.0-0`, o("tilde return", P), P;
    });
  }, N = (v, m) => v.trim().split(/\s+/).map((y) => q(y, m)).join(" "), q = (v, m) => {
    o("caret", v, m);
    const y = m.loose ? c[s.CARETLOOSE] : c[s.CARET], E = m.includePrerelease ? "-0" : "";
    return v.replace(y, (R, w, D, F, P) => {
      o("caret", v, R, w, D, F, P);
      let B;
      return A(w) ? B = "" : A(D) ? B = `>=${w}.0.0${E} <${+w + 1}.0.0-0` : A(F) ? w === "0" ? B = `>=${w}.${D}.0${E} <${w}.${+D + 1}.0-0` : B = `>=${w}.${D}.0${E} <${+w + 1}.0.0-0` : P ? (o("replaceCaret pr", P), w === "0" ? D === "0" ? B = `>=${w}.${D}.${F}-${P} <${w}.${D}.${+F + 1}-0` : B = `>=${w}.${D}.${F}-${P} <${w}.${+D + 1}.0-0` : B = `>=${w}.${D}.${F}-${P} <${+w + 1}.0.0-0`) : (o("no pr"), w === "0" ? D === "0" ? B = `>=${w}.${D}.${F}${E} <${w}.${D}.${+F + 1}-0` : B = `>=${w}.${D}.${F}${E} <${w}.${+D + 1}.0-0` : B = `>=${w}.${D}.${F} <${+w + 1}.0.0-0`), o("caret return", B), B;
    });
  }, T = (v, m) => (o("replaceXRanges", v, m), v.split(/\s+/).map((y) => J(y, m)).join(" ")), J = (v, m) => {
    v = v.trim();
    const y = m.loose ? c[s.XRANGELOOSE] : c[s.XRANGE];
    return v.replace(y, (E, R, w, D, F, P) => {
      o("xRange", v, E, R, w, D, F, P);
      const B = A(w), re = B || A(D), W = re || A(F), ze = W;
      return R === "=" && ze && (R = ""), P = m.includePrerelease ? "-0" : "", B ? R === ">" || R === "<" ? E = "<0.0.0-0" : E = "*" : R && ze ? (re && (D = 0), F = 0, R === ">" ? (R = ">=", re ? (w = +w + 1, D = 0, F = 0) : (D = +D + 1, F = 0)) : R === "<=" && (R = "<", re ? w = +w + 1 : D = +D + 1), R === "<" && (P = "-0"), E = `${R + w}.${D}.${F}${P}`) : re ? E = `>=${w}.0.0${P} <${+w + 1}.0.0-0` : W && (E = `>=${w}.${D}.0${P} <${w}.${+D + 1}.0-0`), o("xRange return", E), E;
    });
  }, Re = (v, m) => (o("replaceStars", v, m), v.trim().replace(c[s.STAR], "")), he = (v, m) => (o("replaceGTE0", v, m), v.trim().replace(c[m.includePrerelease ? s.GTE0PRE : s.GTE0], "")), Ie = (v) => (m, y, E, R, w, D, F, P, B, re, W, ze) => (A(E) ? y = "" : A(R) ? y = `>=${E}.0.0${v ? "-0" : ""}` : A(w) ? y = `>=${E}.${R}.0${v ? "-0" : ""}` : D ? y = `>=${y}` : y = `>=${y}${v ? "-0" : ""}`, A(B) ? P = "" : A(re) ? P = `<${+B + 1}.0.0-0` : A(W) ? P = `<${B}.${+re + 1}.0-0` : ze ? P = `<=${B}.${re}.${W}-${ze}` : v ? P = `<${B}.${re}.${+W + 1}-0` : P = `<=${P}`, `${y} ${P}`.trim()), ce = (v, m, y) => {
    for (let E = 0; E < v.length; E++)
      if (!v[E].test(m))
        return !1;
    if (m.prerelease.length && !y.includePrerelease) {
      for (let E = 0; E < v.length; E++)
        if (o(v[E].semver), v[E].semver !== a.ANY && v[E].semver.prerelease.length > 0) {
          const R = v[E].semver;
          if (R.major === m.major && R.minor === m.minor && R.patch === m.patch)
            return !0;
        }
      return !1;
    }
    return !0;
  };
  return Lr;
}
var Mr, mi;
function Zt() {
  if (mi) return Mr;
  mi = 1;
  const t = Symbol("SemVer ANY");
  class e {
    static get ANY() {
      return t;
    }
    constructor(l, u) {
      if (u = r(u), l instanceof e) {
        if (l.loose === !!u.loose)
          return l;
        l = l.value;
      }
      l = l.trim().split(/\s+/).join(" "), o("comparator", l, u), this.options = u, this.loose = !!u.loose, this.parse(l), this.semver === t ? this.value = "" : this.value = this.operator + this.semver.version, o("comp", this);
    }
    parse(l) {
      const u = this.options.loose ? n[i.COMPARATORLOOSE] : n[i.COMPARATOR], h = l.match(u);
      if (!h)
        throw new TypeError(`Invalid comparator: ${l}`);
      this.operator = h[1] !== void 0 ? h[1] : "", this.operator === "=" && (this.operator = ""), h[2] ? this.semver = new d(h[2], this.options.loose) : this.semver = t;
    }
    toString() {
      return this.value;
    }
    test(l) {
      if (o("Comparator.test", l, this.options.loose), this.semver === t || l === t)
        return !0;
      if (typeof l == "string")
        try {
          l = new d(l, this.options);
        } catch {
          return !1;
        }
      return a(l, this.operator, this.semver, this.options);
    }
    intersects(l, u) {
      if (!(l instanceof e))
        throw new TypeError("a Comparator is required");
      return this.operator === "" ? this.value === "" ? !0 : new c(l.value, u).test(this.value) : l.operator === "" ? l.value === "" ? !0 : new c(this.value, u).test(l.semver) : (u = r(u), u.includePrerelease && (this.value === "<0.0.0-0" || l.value === "<0.0.0-0") || !u.includePrerelease && (this.value.startsWith("<0.0.0") || l.value.startsWith("<0.0.0")) ? !1 : !!(this.operator.startsWith(">") && l.operator.startsWith(">") || this.operator.startsWith("<") && l.operator.startsWith("<") || this.semver.version === l.semver.version && this.operator.includes("=") && l.operator.includes("=") || a(this.semver, "<", l.semver, u) && this.operator.startsWith(">") && l.operator.startsWith("<") || a(this.semver, ">", l.semver, u) && this.operator.startsWith("<") && l.operator.startsWith(">")));
    }
  }
  Mr = e;
  const r = vn(), { safeRe: n, t: i } = ut(), a = ia(), o = Bt(), d = ae(), c = Ee();
  return Mr;
}
var Nr, gi;
function Gt() {
  if (gi) return Nr;
  gi = 1;
  const t = Ee();
  return Nr = (r, n, i) => {
    try {
      n = new t(n, i);
    } catch {
      return !1;
    }
    return n.test(r);
  }, Nr;
}
var Hr, vi;
function Xo() {
  if (vi) return Hr;
  vi = 1;
  const t = Ee();
  return Hr = (r, n) => new t(r, n).set.map((i) => i.map((a) => a.value).join(" ").trim().split(" ")), Hr;
}
var kr, bi;
function Wo() {
  if (bi) return kr;
  bi = 1;
  const t = ae(), e = Ee();
  return kr = (n, i, a) => {
    let o = null, d = null, c = null;
    try {
      c = new e(i, a);
    } catch {
      return null;
    }
    return n.forEach((s) => {
      c.test(s) && (!o || d.compare(s) === -1) && (o = s, d = new t(o, a));
    }), o;
  }, kr;
}
var Fr, Ei;
function Yo() {
  if (Ei) return Fr;
  Ei = 1;
  const t = ae(), e = Ee();
  return Fr = (n, i, a) => {
    let o = null, d = null, c = null;
    try {
      c = new e(i, a);
    } catch {
      return null;
    }
    return n.forEach((s) => {
      c.test(s) && (!o || d.compare(s) === 1) && (o = s, d = new t(o, a));
    }), o;
  }, Fr;
}
var Vr, yi;
function zo() {
  if (yi) return Vr;
  yi = 1;
  const t = ae(), e = Ee(), r = jt();
  return Vr = (i, a) => {
    i = new e(i, a);
    let o = new t("0.0.0");
    if (i.test(o) || (o = new t("0.0.0-0"), i.test(o)))
      return o;
    o = null;
    for (let d = 0; d < i.set.length; ++d) {
      const c = i.set[d];
      let s = null;
      c.forEach((l) => {
        const u = new t(l.semver.version);
        switch (l.operator) {
          case ">":
            u.prerelease.length === 0 ? u.patch++ : u.prerelease.push(0), u.raw = u.format();
          /* fallthrough */
          case "":
          case ">=":
            (!s || r(u, s)) && (s = u);
            break;
          case "<":
          case "<=":
            break;
          /* istanbul ignore next */
          default:
            throw new Error(`Unexpected operation: ${l.operator}`);
        }
      }), s && (!o || r(o, s)) && (o = s);
    }
    return o && i.test(o) ? o : null;
  }, Vr;
}
var qr, wi;
function Jo() {
  if (wi) return qr;
  wi = 1;
  const t = Ee();
  return qr = (r, n) => {
    try {
      return new t(r, n).range || "*";
    } catch {
      return null;
    }
  }, qr;
}
var Br, Ri;
function Rn() {
  if (Ri) return Br;
  Ri = 1;
  const t = ae(), e = Zt(), { ANY: r } = e, n = Ee(), i = Gt(), a = jt(), o = En(), d = wn(), c = yn();
  return Br = (l, u, h, p) => {
    l = new t(l, p), u = new n(u, p);
    let b, f, I, _, k;
    switch (h) {
      case ">":
        b = a, f = d, I = o, _ = ">", k = ">=";
        break;
      case "<":
        b = o, f = c, I = a, _ = "<", k = "<=";
        break;
      default:
        throw new TypeError('Must provide a hilo val of "<" or ">"');
    }
    if (i(l, u, p))
      return !1;
    for (let A = 0; A < u.set.length; ++A) {
      const V = u.set[A];
      let G = null, N = null;
      if (V.forEach((q) => {
        q.semver === r && (q = new e(">=0.0.0")), G = G || q, N = N || q, b(q.semver, G.semver, p) ? G = q : I(q.semver, N.semver, p) && (N = q);
      }), G.operator === _ || G.operator === k || (!N.operator || N.operator === _) && f(l, N.semver))
        return !1;
      if (N.operator === k && I(l, N.semver))
        return !1;
    }
    return !0;
  }, Br;
}
var jr, Ii;
function Ko() {
  if (Ii) return jr;
  Ii = 1;
  const t = Rn();
  return jr = (r, n, i) => t(r, n, ">", i), jr;
}
var Zr, Si;
function Qo() {
  if (Si) return Zr;
  Si = 1;
  const t = Rn();
  return Zr = (r, n, i) => t(r, n, "<", i), Zr;
}
var Gr, $i;
function es() {
  if ($i) return Gr;
  $i = 1;
  const t = Ee();
  return Gr = (r, n, i) => (r = new t(r, i), n = new t(n, i), r.intersects(n, i)), Gr;
}
var Ur, Ti;
function ts() {
  if (Ti) return Ur;
  Ti = 1;
  const t = Gt(), e = be();
  return Ur = (r, n, i) => {
    const a = [];
    let o = null, d = null;
    const c = r.sort((h, p) => e(h, p, i));
    for (const h of c)
      t(h, n, i) ? (d = h, o || (o = h)) : (d && a.push([o, d]), d = null, o = null);
    o && a.push([o, null]);
    const s = [];
    for (const [h, p] of a)
      h === p ? s.push(h) : !p && h === c[0] ? s.push("*") : p ? h === c[0] ? s.push(`<=${p}`) : s.push(`${h} - ${p}`) : s.push(`>=${h}`);
    const l = s.join(" || "), u = typeof n.raw == "string" ? n.raw : String(n);
    return l.length < u.length ? l : n;
  }, Ur;
}
var Xr, xi;
function rs() {
  if (xi) return Xr;
  xi = 1;
  const t = Ee(), e = Zt(), { ANY: r } = e, n = Gt(), i = be(), a = (u, h, p = {}) => {
    if (u === h)
      return !0;
    u = new t(u, p), h = new t(h, p);
    let b = !1;
    e: for (const f of u.set) {
      for (const I of h.set) {
        const _ = c(f, I, p);
        if (b = b || _ !== null, _)
          continue e;
      }
      if (b)
        return !1;
    }
    return !0;
  }, o = [new e(">=0.0.0-0")], d = [new e(">=0.0.0")], c = (u, h, p) => {
    if (u === h)
      return !0;
    if (u.length === 1 && u[0].semver === r) {
      if (h.length === 1 && h[0].semver === r)
        return !0;
      p.includePrerelease ? u = o : u = d;
    }
    if (h.length === 1 && h[0].semver === r) {
      if (p.includePrerelease)
        return !0;
      h = d;
    }
    const b = /* @__PURE__ */ new Set();
    let f, I;
    for (const T of u)
      T.operator === ">" || T.operator === ">=" ? f = s(f, T, p) : T.operator === "<" || T.operator === "<=" ? I = l(I, T, p) : b.add(T.semver);
    if (b.size > 1)
      return null;
    let _;
    if (f && I) {
      if (_ = i(f.semver, I.semver, p), _ > 0)
        return null;
      if (_ === 0 && (f.operator !== ">=" || I.operator !== "<="))
        return null;
    }
    for (const T of b) {
      if (f && !n(T, String(f), p) || I && !n(T, String(I), p))
        return null;
      for (const J of h)
        if (!n(T, String(J), p))
          return !1;
      return !0;
    }
    let k, A, V, G, N = I && !p.includePrerelease && I.semver.prerelease.length ? I.semver : !1, q = f && !p.includePrerelease && f.semver.prerelease.length ? f.semver : !1;
    N && N.prerelease.length === 1 && I.operator === "<" && N.prerelease[0] === 0 && (N = !1);
    for (const T of h) {
      if (G = G || T.operator === ">" || T.operator === ">=", V = V || T.operator === "<" || T.operator === "<=", f) {
        if (q && T.semver.prerelease && T.semver.prerelease.length && T.semver.major === q.major && T.semver.minor === q.minor && T.semver.patch === q.patch && (q = !1), T.operator === ">" || T.operator === ">=") {
          if (k = s(f, T, p), k === T && k !== f)
            return !1;
        } else if (f.operator === ">=" && !n(f.semver, String(T), p))
          return !1;
      }
      if (I) {
        if (N && T.semver.prerelease && T.semver.prerelease.length && T.semver.major === N.major && T.semver.minor === N.minor && T.semver.patch === N.patch && (N = !1), T.operator === "<" || T.operator === "<=") {
          if (A = l(I, T, p), A === T && A !== I)
            return !1;
        } else if (I.operator === "<=" && !n(I.semver, String(T), p))
          return !1;
      }
      if (!T.operator && (I || f) && _ !== 0)
        return !1;
    }
    return !(f && V && !I && _ !== 0 || I && G && !f && _ !== 0 || q || N);
  }, s = (u, h, p) => {
    if (!u)
      return h;
    const b = i(u.semver, h.semver, p);
    return b > 0 ? u : b < 0 || h.operator === ">" && u.operator === ">=" ? h : u;
  }, l = (u, h, p) => {
    if (!u)
      return h;
    const b = i(u.semver, h.semver, p);
    return b < 0 ? u : b > 0 || h.operator === "<" && u.operator === "<=" ? h : u;
  };
  return Xr = a, Xr;
}
var Wr, Ci;
function ns() {
  if (Ci) return Wr;
  Ci = 1;
  const t = ut(), e = qt(), r = ae(), n = ta(), i = Ye(), a = _o(), o = Lo(), d = Mo(), c = No(), s = Ho(), l = ko(), u = Fo(), h = Vo(), p = be(), b = qo(), f = Bo(), I = bn(), _ = jo(), k = Zo(), A = jt(), V = En(), G = ra(), N = na(), q = yn(), T = wn(), J = ia(), Re = Go(), he = Zt(), Ie = Ee(), ce = Gt(), v = Xo(), m = Wo(), y = Yo(), E = zo(), R = Jo(), w = Rn(), D = Ko(), F = Qo(), P = es(), B = ts(), re = rs();
  return Wr = {
    parse: i,
    valid: a,
    clean: o,
    inc: d,
    diff: c,
    major: s,
    minor: l,
    patch: u,
    prerelease: h,
    compare: p,
    rcompare: b,
    compareLoose: f,
    compareBuild: I,
    sort: _,
    rsort: k,
    gt: A,
    lt: V,
    eq: G,
    neq: N,
    gte: q,
    lte: T,
    cmp: J,
    coerce: Re,
    Comparator: he,
    Range: Ie,
    satisfies: ce,
    toComparators: v,
    maxSatisfying: m,
    minSatisfying: y,
    minVersion: E,
    validRange: R,
    outside: w,
    gtr: D,
    ltr: F,
    intersects: P,
    simplifyRange: B,
    subset: re,
    SemVer: r,
    re: t.re,
    src: t.src,
    tokens: t.t,
    SEMVER_SPEC_VERSION: e.SEMVER_SPEC_VERSION,
    RELEASE_TYPES: e.RELEASE_TYPES,
    compareIdentifiers: n.compareIdentifiers,
    rcompareIdentifiers: n.rcompareIdentifiers
  }, Wr;
}
var is = ns();
const ot = /* @__PURE__ */ Ra(is), Di = (t) => ot.valid(t) ?? ot.valid(ot.coerce(t)), as = window.Vaadin.copilot.eventbus;
as.on("serverInfo", (t) => {
  const r = t.detail.versions.find((a) => a.name === "Vaadin");
  if (!r)
    return;
  const n = je.getMostRecentVaadinVersion(), i = r.version;
  Ai(`${Ia}get-release-note-url`, { version: i }, (a) => {
    const o = a.data;
    if (!o.error && o.url)
      if (n) {
        const d = Di(i), c = Di(n), s = d !== null && c !== null ? ot.gte(d, c) : i === n;
        O.setProjectVersionReleaseNoteInfo({
          mostRecentVersion: s,
          vaadinVersion: i,
          url: o.url
        }), d !== null && c !== null && ot.gt(d, c) && (Pe({
          type: se.INFORMATION,
          message: `You're now on version ${i}!`,
          details: Nt(
            C`<a class="ahreflike" href="${o.url}" target="_blank">Click here to open release notes!</a>`
          )
        }), je.setMostRecentReleaseNoteDismissed(!1), je.setMostRecentVaadinVersion(i));
      } else
        je.setMostRecentReleaseNoteDismissed(!1), je.setMostRecentVaadinVersion(i), O.setProjectVersionReleaseNoteInfo({
          mostRecentVersion: !0,
          vaadinVersion: i,
          url: o.url
        });
  });
});
X.on("copilot-java-after-update", (t) => {
  t.detail.classes.find((e) => e.routePath !== void 0) && X.emit("update-routes", {});
});
