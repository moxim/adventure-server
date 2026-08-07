import { i as e, n as t, t as n } from "./chunk-DiqZc92J.js";
import { $ as r, C as i, D as a, J as o, L as s, M as c, N as l, R as u, at as d, ct as f, et as p, h as m, i as h, n as g, o as _, r as v, t as y, u as b, ut as x, v as ee, x as te, y as ne, z as S } from "./icons-Daxdm7Mp.js";
import { l as re, o as ie, t as C } from "./consts-PqkJ5Usg.js";
import { c as ae, r as oe } from "./dom-utils-U1Di7XbE.js";
import { a as se, o as ce } from "./copilot-tree-impl-Bh3d7r5l.js";
import { a as w, c as le, d as ue, i as de, l as T, n as E, o as fe, r as pe, s as me, t as he, u as ge } from "./section-panel-ui-state-ByeyD6ou.js";
import { n as _e, t as D } from "./copilot-eventbus-tR06exIW.js";
import { d as ve, i as ye, l as be, n as xe, t as Se, u as Ce } from "./copilot-modes-BbD7YWYd.js";
import { n as O, r as we, t as Te } from "./copilot-ui-state-DjlBBpyf.js";
import { r as Ee, t as De } from "./stats-D_j8yo6z.js";
import { a as Oe, c as ke, i as Ae, l as je, n as Me, o as Ne, r as Pe, s as Fe, t as Ie, u as Le } from "./copilot-userinfo-aSfT50j2.js";
import { t as Re } from "./directive-CZ105jp5.js";
import { d as ze, f as Be } from "./copilot-error-handler-B7az3wX2.js";
import { n as Ve, t as He } from "./copilot-stored-machine-state-DS3t0BPl.js";
import { n as Ue, r as We, t as Ge } from "./copilot-notification-BnxWYvla.js";
import { n as Ke, t as k } from "./early-project-state-D-4_8bD-.js";
import { i as qe, o as Je, s as Ye } from "./copilot-development-setup-user-guide-utils-BxGBMF0F.js";
import { n as Xe, t as Ze } from "./track-active-mode-event-zQOkAKHr.js";
import { n as Qe, r as $e, t as et } from "./lit-renderer-Ca0EZVmm.js";
import { n as tt, r as nt, t as rt } from "./copy-to-clipboard-B1yKQNyU.js";
//#region node_modules/@vaadin/component-base/src/async.js
function it() {
	ct = !1;
	let e = st.length;
	for (let t = 0; t < e; t++) {
		let e = st[t];
		if (e) try {
			e();
		} catch (e) {
			setTimeout(() => {
				throw e;
			});
		}
	}
	st.splice(0, e), ot += e;
}
var at, ot, st, ct, lt, ut = t((() => {
	at = 0, ot = 0, st = [], ct = !1, lt = {
		run(e) {
			ct || (ct = !0, queueMicrotask(() => it())), st.push(e);
			let t = at;
			return at += 1, t;
		},
		cancel(e) {
			let t = e - ot;
			if (t >= 0) {
				if (!st[t]) throw Error(`invalid async handle: ${e}`);
				st[t] = null;
			}
		}
	};
})), dt, ft, pt = t((() => {
	dt = /* @__PURE__ */ new Set(), ft = class e {
		static debounce(t, n, r) {
			return t instanceof e ? t._cancelAsync() : t = new e(), t.setConfig(n, r), t;
		}
		constructor() {
			this._asyncModule = null, this._callback = null, this._timer = null;
		}
		setConfig(e, t) {
			this._asyncModule = e, this._callback = t, this._timer = this._asyncModule.run(() => {
				this._timer = null, dt.delete(this), this._callback();
			});
		}
		cancel() {
			this.isActive() && (this._cancelAsync(), dt.delete(this));
		}
		_cancelAsync() {
			this.isActive() && (this._asyncModule.cancel(this._timer), this._timer = null);
		}
		flush() {
			this.isActive() && (this.cancel(), this._callback());
		}
		isActive() {
			return this._timer != null;
		}
	};
})), mt, ht, gt, _t, vt, yt, bt, xt, St = t((() => {
	$e(), ut(), pt(), Qe(), mt = Symbol("contentUpdateDebouncer"), ht = class extends et {
		get rendererProperty() {
			throw Error("The `rendererProperty` getter must be implemented.");
		}
		addRenderer() {
			this.element[this.rendererProperty] = (e, t) => {
				this.renderRenderer(e, t);
			};
		}
		runRenderer() {
			this.element[mt] = ft.debounce(this.element[mt], lt, () => {
				this.element.requestContentUpdate();
			});
		}
		removeRenderer() {
			this.element[this.rendererProperty] = null, delete this.element[mt];
		}
	}, gt = class extends ht {
		get rendererProperty() {
			return "renderer";
		}
	}, _t = class extends ht {
		get rendererProperty() {
			return "headerRenderer";
		}
	}, vt = class extends ht {
		get rendererProperty() {
			return "footerRenderer";
		}
	}, yt = Re(gt), bt = Re(_t), xt = Re(vt);
})), Ct = t((() => {
	St();
})), wt, Tt = t((() => {
	de(), me(), he(), v(), tt(), Ct(), g(), ze(), we(), ae(), re(), d(), fe(), wt = class extends pe {
		constructor(...e) {
			super(...e), this.panelHeaderUpdated = 0, this.openedPanelConfigurationsSorted = [], this.windowResizeListener = async () => {
				await Promise.all(this.getOpenPanelContainerDialogs().map(async (e) => {
					let t = e.getAttribute("data-panel-tag");
					if (!t) return;
					await e.querySelector(t)?.requestLayoutUpdate();
					let n = E.getPanelByTag(t);
					if (!n?.position) return;
					let r = e.getBoundingClientRect(), i = this.getViewportAdjustedPosition({
						...n.position,
						width: n.position.width ?? r.width,
						height: n.position.height ?? r.height
					});
					e.setAttribute("top", `${i.top}`), e.setAttribute("left", `${i.left}`), e.setAttribute("width", `${i.width}`), e.setAttribute("height", `${i.height}`), E.updatePanel(n.tag, { position: i }, !1);
				}));
			};
		}
		connectedCallback() {
			super.connectedCallback(), window.addEventListener("resize", this.windowResizeListener), this.reaction(() => O.operationInProgress, () => {
				Array.from(this.querySelectorAll("vaadin-dialog[panel-container]")).forEach((e) => {
					let t = e.hasAttribute("showWhileDragging");
					requestAnimationFrame(() => {
						e.toggleAttribute("hiding-while-drag-and-drop", O.operationInProgress === Te.DragAndDrop && !t && !this.hasDropTarget(e));
					});
				});
			}), this.reaction(() => E.getAttentionRequiredPanelConfiguration(), () => {
				let e = E.getAttentionRequiredPanelConfiguration(), t = this.getDialogByPanelTag(e?.tag);
				t && t.toggleAttribute(C, !0);
			}), this.observeDisposer = f(E.getCustomPanelHeaderMap(), () => {
				this.panelHeaderUpdated += 1;
			}), this.reaction(() => [
				E.panels,
				E.panelStackingOrder,
				E.getOpenPanelTags().size
			], () => {
				let e = E.panels.filter((e) => E.isOpenedPanel(e.tag)), t = E.panelStackingOrder;
				this.openedPanelConfigurationsSorted = e.sort((e, n) => t.indexOf(e.tag) - t.indexOf(n.tag));
			}, { fireImmediately: !0 });
		}
		disconnectedCallback() {
			super.disconnectedCallback(), window.removeEventListener("resize", this.windowResizeListener), this.observeDisposer && this.observeDisposer();
		}
		createRenderRoot() {
			return this;
		}
		render() {
			return b`
      ${nt(this.openedPanelConfigurationsSorted, (e) => e.tag, (e) => b`<vaadin-dialog
            draggable
            modeless
            id="${e.tag}-dialog"
            waits-positioning
            ?individual="${e.individual}"
            .opened="${E.isOpenedPanel(e.tag)}"
            panel-container
            resizable
            @mousedown="${(t) => {
				E.moveStackingOrderToTop(e.tag);
			}}"
            @touchstart="${(t) => {
				E.moveStackingOrderToTop(e.tag);
			}}"
            data-panel-tag="${e.tag}"
            ?showWhileDragging="${e.showWhileDragging}"
            theme="no-padding"
            left="${e.position?.left}"
            top="${e.position?.top}"
            height=${e.position?.height}
            width=${e.position?.width}
            @resize="${(t) => {
				let { top: n, left: r, width: i, height: a } = t.detail, o = t.currentTarget, s = Number.parseFloat(i), c = Number.parseFloat(a), l = this.getViewportAdjustedPosition({
					top: Number.parseFloat(n),
					left: Number.parseFloat(r),
					width: Number.isFinite(s) ? s : o.getBoundingClientRect().width,
					height: Number.isFinite(c) ? c : o.getBoundingClientRect().height
				});
				o.setAttribute("top", `${l.top}`), o.setAttribute("left", `${l.left}`), o.setAttribute("width", `${l.width}`), o.setAttribute("height", `${l.height}`), E.updatePanel(e.tag, { position: l });
			}}"
            @dragged="${(t) => {
				let n = E.getPanelByTag(e.tag);
				if (!n) return;
				let r = t.currentTarget, i = (r._overlayElement?.$?.overlay).getBoundingClientRect(), a, o;
				if (i ? (a = i.width, o = i.height) : (a = n.position?.width, o = n.position?.height), !a || !o) return;
				let s = {
					top: Number.parseFloat(t.detail.top),
					left: Number.parseFloat(t.detail.left)
				}, c = this.getViewportAdjustedPosition({
					width: i.width,
					height: i.height,
					...n.position,
					...s
				});
				r.setAttribute("top", `${c.top}`), r.setAttribute("left", `${c.left}`), E.updatePanel(n.tag, { position: c });
			}}"
            @mouseenter="${(e) => {
				let t = e.target;
				t.hasAttribute("attention-required") && E.clearAttention(), t.toggleAttribute(C, !1);
			}}"
            @opened-changed="${(t) => {
				let n = t.detail.value, r = t.currentTarget;
				n && (E.moveStackingOrderToTop(e.tag), (r.querySelector(e.tag)?.requestLayoutUpdate() ?? Promise.resolve()).finally(() => {
					setTimeout(() => {
						r.removeAttribute("waits-positioning");
					}, 50);
				}));
				let i = this.getToolbar();
				if (!i) return;
				let a = i.getButtonId(e), o = i.querySelector(`vaadin-button#${a}`);
				o && (n ? o.classList.add("selected") : o.classList.remove("selected"));
			}}"
            ${bt(() => b`
                <h2 class="font-bold me-auto my-0 text-xs truncate uppercase draggable" id="${e.tag}-title">
                  ${E.getPanelHeader(e)}
                  ${e.experimental ? b`<vaadin-icon slot="suffix" .svg="${y.experiment}"></vaadin-icon>
                        <vaadin-tooltip slot="tooltip" text="Experimental feature"></vaadin-tooltip>` : _}
                </h2>
                ${e.actionsTag ? Be(`<${e.actionsTag}></${e.actionsTag}>`) : _}
                ${e.helpUrl ? b`<vaadin-button
                      aria-label="More info about ${e.header}"
                      @click="${() => window.open(e.helpUrl, "_blank")}"
                      @mousedown="${(e) => e.stopPropagation()}"
                      theme="icon tertiary">
                      <vaadin-icon .svg="${y.help}"></vaadin-icon>
                      <vaadin-tooltip slot="tooltip" text="More info about ${e.header}"></vaadin-tooltip>
                    </vaadin-button>` : _}
                <vaadin-button
                  aria-label="Close"
                  @click="${() => {
				let t = this.getDialogByPanelTag(e.tag)?.querySelector(e.tag);
				t?.requestClose ? t.requestClose(() => E.closePanel(e.tag)) : E.closePanel(e.tag);
			}}"
                  @mousedown="${(e) => e.stopPropagation()}"
                  theme="icon tertiary">
                  <vaadin-icon .svg="${y.close}"></vaadin-icon>
                  <vaadin-tooltip slot="tooltip" text="Close"></vaadin-tooltip>
                </vaadin-button>
              `, [this.panelHeaderUpdated])}
            ${yt(() => b`<div class="h-full w-full">${Be(`<${e.tag}></${e.tag}>`)}</div>`, [])}
            ${e.footerActionsTag ? xt(() => b`
                    ${Be(`<${e.footerActionsTag}></${e.footerActionsTag}>`)}
                  `, []) : _}></vaadin-dialog>`)}
    `;
		}
		hasDropTarget(e) {
			let t = e.children;
			for (let e of t) {
				let t = oe(e.shadowRoot ?? e, "copilot-image-upload");
				if (t && getComputedStyle(t).display !== "none") return !0;
			}
			return !1;
		}
		getOpenPanelContainerDialogs() {
			let e = Array.from(this.querySelectorAll("vaadin-dialog[panel-container][opened]")), t = E.panelStackingOrder;
			return e.sort((e, n) => {
				let r = e.dataset.panelTag, i = n.dataset.panelTag;
				return r === null ? i === null ? 0 : 1 : i === null ? -1 : t.indexOf(r) - t.indexOf(i);
			}), e;
		}
		getDialogByPanelTag(e) {
			return e ? this.querySelector(`vaadin-dialog#${e}-dialog`) : null;
		}
		getToolbar() {
			let e = document.querySelector("copilot-main");
			return e ? e.shadowRoot.querySelector("copilot-toolbar") : null;
		}
		getViewportAdjustedPosition(e) {
			let t = this.getViewportPaddingPx(), n = Number.isFinite(e.width) ? e.width : 0, r = Number.isFinite(e.height) ? e.height : 0, i = Number.isFinite(e.top) ? e.top : 0, a = Number.isFinite(e.left) ? e.left : 0, o = a + n, s = i + r, c = 0, l = 0;
			return a < 0 ? c = -a : o > window.innerWidth - t && (c = window.innerWidth - t - o), i < 0 ? l = -i : s > window.innerHeight - t && (l = window.innerHeight - t - s), a += c, i += l, {
				top: i,
				left: a,
				width: n,
				height: r
			};
		}
		getViewportPaddingPx() {
			let e = Number.parseFloat(getComputedStyle(this).fontSize);
			return Number.isFinite(e) ? e : 16;
		}
	}, w([T()], wt.prototype, "panelHeaderUpdated", void 0), w([T()], wt.prototype, "openedPanelConfigurationsSorted", void 0), wt = w([ue("copilot-panel-manager")], wt);
})), Et, Dt = t((() => {
	if (Et = window.Vaadin.copilot.customComponentHandler, !Et) throw Error("Tried to access custom component handler before it was initialized.");
}));
//#endregion
//#region frontend/copilot/copilot-undo-redo-handler.ts
function Ot(e) {
	O.setOperationWaitsHmrUpdate(e, 3e4);
}
function kt(e) {
	if (e.detail.files) return e.detail.files;
	let t = Et.getActiveDrillDownContext();
	if (t) {
		let e = Et.getCustomComponentInfo(t);
		if (e) return Array(e.customComponentFilePath);
	}
	return S();
}
var At = t((() => {
	_e(), o(), l(), De(), we(), Dt(), s(), Ue(), p(), D.on("undoRedo", (e) => {
		let t = {
			files: kt(e),
			uiId: c()
		}, n = e.detail.undo ? "copilot-plugin-undo" : "copilot-plugin-redo", i = e.detail.undo ? "undo" : "redo";
		Ee(i), Ot(Te.RedoUndo), u(n, t, (e) => {
			if (!e.data.performed) {
				if (e.data.error && e.data.error.message) {
					We({
						type: r.ERROR,
						message: e.data.error.message
					}), D.emit("vite-after-update", {});
					return;
				}
				We({
					type: r.INFORMATION,
					message: `Nothing to ${i}`
				}), D.emit("vite-after-update", {});
			}
		});
	});
}));
//#endregion
//#region frontend/copilot/copilot-notifications-container.ts
function jt(e) {
	return e.dontShowAgainMessage ? e.dontShowAgainMessage : "Don't show again";
}
var Mt, Nt = t((() => {
	de(), me(), we(), v(), tt(), Ue(), i(), g(), p(), _e(), fe(), Mt = class extends pe {
		createRenderRoot() {
			return this;
		}
		connectedCallback() {
			super.connectedCallback(), this.classList.add("end-2", "fixed", "top-2", "z-100"), D.on("show-notification", (e) => {
				let t = e.detail.notification;
				We(t);
			});
		}
		render() {
			return b`<section
      aria-label="Notifications"
      aria-live="polite"
      class="flex flex-col-reverse items-end list-none m-0 p-0">
      ${nt(O.notifications, (e) => e.id, (e) => this.renderNotification(e))}
    </section>`;
		}
		renderNotification(e) {
			return b`
      <div
        class="duration-300 origin-top transition-all ${e.animatingIn ? "delay-300 max-h-0 opacity-0 scale-99 -translate-y-2" : e.animatingOut ? "delay-0 max-h-0 opacity-0 scale-99 -translate-y-2" : "max-h-screen"}"
        data-testid="message"
        ?data-error="${e.type === r.ERROR}">
        <div
          class="bg-gray-1 dark:bg-gray-5 border flex gap-2 mb-2 p-3 relative rounded-md shadow-xl ${e.autoWidth ? "" : "w-sm"}">
          <vaadin-icon
            class="${e.type === r.ERROR ? "text-ruby-11" : e.type === r.WARNING ? "text-amber-11" : "text-blue-11"}"
            .svg="${e.type === r.WARNING || e.type === r.ERROR ? y.warning : y.info}"></vaadin-icon>
          <div class="flex flex-col flex-1">
            <h2 class="font-semibold m-0 pe-8 text-sm">${e.message}</h2>
            <div
              class="break-word flex flex-col items-start text-secondary text-xs"
              ?hidden="${!e.details && !e.link}">
              ${a(e.details)}
              ${e.link ? b`<a class="ahreflike" href="${e.link}" target="_blank">Learn more</a>` : ""}
            </div>
            ${e.dismissId ? b` <hr class="border-b border-e-0 border-s-0 border-t-0 mx-0 my-3 w-full" />
                  <vaadin-checkbox
                    ?checked=${e.dontShowAgain}
                    @change=${() => this.toggleDontShowAgain(e)}
                    label="${jt(e)}">
                  </vaadin-checkbox>` : ""}
          </div>
          <vaadin-button
            aria-label="Close"
            class="absolute end-1.5 top-1.5"
            @click=${(t) => {
				Ge(e), t.stopPropagation();
			}}
            id="dismiss"
            theme="icon tertiary">
            <vaadin-icon .svg="${y.close}"></vaadin-icon>
            <vaadin-tooltip slot="tooltip" text="Close"></vaadin-tooltip>
          </vaadin-button>
        </div>
      </div>
    `;
		}
		toggleDontShowAgain(e) {
			e.dontShowAgain = !e.dontShowAgain, this.requestUpdate();
		}
	}, Mt = w([ue("copilot-notifications-container")], Mt);
})), Pt = t((() => {
	Ue(), p(), We({
		type: r.INFORMATION,
		autoWidth: !0,
		message: "App is running in development mode",
		dismissId: "devmode"
	});
}));
//#endregion
//#region frontend/copilot/update-listeners.ts
function Ft() {
	xe().default || (Lt.clear(), Lt(), Pe());
}
var It, Lt, Rt = t((() => {
	if (_e(), we(), It = /* @__PURE__ */ e(Ne(), 1), Ue(), p(), Oe(), Ke(), Ye(), De(), ye(), i(), Lt = (0, It.default)(async () => {
		await Ae();
	}, 100), D.on("location-changed", () => {
		Lt();
	}), window.addEventListener("vaadin-navigated", () => {
		xe().default || Lt();
	}), window.addEventListener("popstate", () => {
		xe().default || Lt();
	}), D.on("vite-after-update", () => {
		xe().default || Lt();
	}), window.__REACT_DEVTOOLS_GLOBAL_HOOK__) {
		let e = window.__REACT_DEVTOOLS_GLOBAL_HOOK__, t = e.onCommitFiberRoot;
		e.onCommitFiberRoot = (e, n, r, i) => (Ft(), t(e, n, r, i));
	}
	ee(() => {
		let e = window?.Vaadin?.connectionState;
		if (e) return e;
	}).then((e) => {
		e.addStateChangeListener && typeof e.addStateChangeListener == "function" ? e.addStateChangeListener((e, t) => {
			e === "loading" && t === "connected" && !xe().default && Ft();
		}) : console.error("Unable to add listener for connection state changes");
	}), D.on("copilot-plugin-state", (e) => {
		O.setIdePluginState(e.detail), e.preventDefault();
	}), D.on("copilot-early-project-state", (e) => {
		k.setSpringSecurityEnabled(e.detail.springSecurityEnabled), k.setSpringJpaDataEnabled(e.detail.springJpaDataEnabled), k.setSpringJpaDatasourceInitialization(e.detail.springJpaDatasourceInitialization), k.setSupportsHilla(e.detail.supportsHilla), k.setSpringApplication(e.detail.springApplication), k.setUrlPrefix(e.detail.urlPrefix), k.setServerVersions(e.detail.serverVersions), k.setJdkInfo(e.detail.jdkInfo), qe() === "success" && Ee("hotswap-active", { value: Je() }), e.preventDefault();
	}), D.on("copilot-ide-notification", (e) => {
		We({
			type: r[e.detail.type],
			message: e.detail.message,
			dismissId: e.detail.dismissId
		}), e.preventDefault();
	});
})), zt, Bt = t((() => {
	me(), v(), Ct(), g(), ye(), fe(), zt = class extends h {
		constructor(...e) {
			super(...e), this.rememberChoice = !1, this.opened = !1, this.handleESC = (e) => {
				xe().appInteractable || !this.opened || (e.key === "Escape" && this.sendEvent("cancel"), e.preventDefault(), e.stopPropagation());
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
			return b` <vaadin-dialog
      id="ai-dialog"
      no-close-on-outside-click
      class="ai-dialog"
      ?opened=${this.opened}
      ${bt(() => b`
          <h2>This Operation Uses AI</h2>
          <vaadin-icon .svg="${y.sparkles}"></vaadin-icon>
        `)}
      ${yt(() => b`
          <p>AI is a third-party service that will receive some of your project code as context for the operation.</p>
          <label>
            <input
              id="ai-dialog-checkbox"
              type="checkbox"
              @change=${(e) => {
				this.rememberChoice = e.target.checked;
			}} />Don’t ask again
          </label>
        `)}
      ${xt(() => b`
          <button @click=${() => this.sendEvent("cancel")}>Cancel</button>
          <button class="primary" @click=${() => this.sendEvent("ok")}>OK</button>
        `)}></vaadin-dialog>`;
		}
		sendEvent(e) {
			this.dispatchEvent(new CustomEvent("ai-usage-response", { detail: {
				response: e,
				rememberChoice: this.rememberChoice
			} }));
		}
		addESCListener() {
			document.addEventListener("keydown", this.handleESC, { capture: !0 });
		}
		removeESCListener() {
			document.removeEventListener("keydown", this.handleESC, { capture: !0 });
		}
	}, w([ge()], zt.prototype, "opened", void 0), zt = w([ue("copilot-ai-usage-confirmation-dialog")], zt);
})), Vt, Ht, Ut, Wt = t((() => {
	v(), me(), Ct(), _e(), Vt = /* @__PURE__ */ e(rt(), 1), fe(), Ht = {
		info: "UI state info",
		stacktrace: "Exception details",
		versions: "Vaadin, Java, OS, etc.."
	}, Ut = class extends h {
		constructor(...e) {
			super(...e), this.exceptionReport = void 0, this.dialogOpened = !1, this.visibleItemIndex = 0, this.versions = void 0, this.selectedItems = [], this.eventListener = (e) => {
				this.exceptionReport = e.detail, this.selectedItems = this.exceptionReport.items.map((e, t) => t), this.visibleItemIndex = 0, this.searchInputValue = void 0, this.dialogOpened = this.exceptionReport !== void 0;
			};
		}
		connectedCallback() {
			super.connectedCallback(), D.on("submit-exception-report-clicked", this.eventListener);
		}
		createRenderRoot() {
			return this;
		}
		disconnectedCallback() {
			super.disconnectedCallback(), D.off("submit-exception-report-clicked", this.eventListener);
		}
		close() {
			this.dialogOpened = !1;
		}
		clear() {
			this.exceptionReport = void 0;
		}
		render() {
			let e = "";
			return this.exceptionReport && this.exceptionReport.items.length > 0 && (e = this.exceptionReport.items[this.visibleItemIndex].content), b` <vaadin-dialog
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
      ${bt(() => b`
          <div
            class="draggable"
            style="display: flex; justify-content: space-between; align-items: center; width: 100%">
            <h2>Send report</h2>
            <vaadin-button theme="tertiary" @click="${this.close}">
              <vaadin-icon icon="lumo:cross"></vaadin-icon>
            </vaadin-button>
          </div>
        `)}
      ${yt(() => b`
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
                ${this.exceptionReport?.items.map((e, t) => b` <vaadin-item>
                      <input
                        type="checkbox"
                        .checked="${this.selectedItems.indexOf(t) !== -1}"
                        @change="${(e) => {
				let n = e.target, r = [...this.selectedItems];
				if (n.checked) r.push(t), this.selectedItems = [...this.selectedItems];
				else {
					let e = this.selectedItems.indexOf(t);
					r.splice(e, 1);
				}
				this.selectedItems = r;
			}}" />
                      <div class="item-content">
                        <span class="item-name"> ${e.name} </span>
                        <span class="item-description">${this.renderItemDescription(e)}</span>
                      </div>
                    </vaadin-item>`)}
              </vaadin-list-box>
            </div>
            <div class="right-menu">
              <div class="section-title">Preview: ${this.exceptionReport?.items[this.visibleItemIndex].name}</div>
              <code class="codeblock">${e}</code>
            </div>
          </div>
        `, [
				this.exceptionReport,
				this.visibleItemIndex,
				this.selectedItems
			])}
      ${xt(() => b`
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
        `, [
				this.exceptionReport,
				this.selectedItems,
				this.searchInputValue
			])}></vaadin-dialog>`;
		}
		renderItemDescription(e) {
			return Object.keys(Ht).indexOf(e.name.toLowerCase()) === -1 ? null : Ht[e.name.toLowerCase()];
		}
		bodyLengthExceeds() {
			let e = this.getIssueBodyNotEncoded();
			return e !== void 0 && encodeURIComponent(e).length > 7500;
		}
		getIssueBodyNotEncoded() {
			if (!this.exceptionReport) return;
			let e = this.exceptionReport.items.filter((e, t) => this.selectedItems.indexOf(t) !== -1).map((e) => {
				let t = "```";
				return e.name.includes(".java") && (t = `${t}java`), `## ${e.name} \n \n ${t}\n${e.content}\n\`\`\``;
			});
			return this.searchInputValue ? `## Description of the bug \n ${this.searchInputValue} \n ${e.join("\n")}` : `## Description of the bug \n Please enter bug description here \n ${e.join("\n")}`;
		}
		submitErrorToGithub() {
			let e = this.exceptionReport;
			if (!e) return;
			let t = encodeURIComponent(e.title ?? "Bug report "), n = this.getIssueBodyNotEncoded();
			if (!n) return;
			let r = encodeURIComponent(n);
			r.length >= 7500 && ((0, Vt.default)(n), r = "Please%20paste%20report%20here.%20It%20was%20automatically%20added%20to%20your%20clipboard.");
			let i = `https://github.com/vaadin/copilot/issues/new?title=${t}&body=${r}`;
			window.open(i, "_blank");
		}
	}, w([T()], Ut.prototype, "exceptionReport", void 0), w([T()], Ut.prototype, "dialogOpened", void 0), w([T()], Ut.prototype, "visibleItemIndex", void 0), w([T()], Ut.prototype, "versions", void 0), w([T()], Ut.prototype, "selectedItems", void 0), w([T()], Ut.prototype, "searchInputValue", void 0), Ut = w([ue("copilot-report-exception-dialog")], Ut);
})), Gt, Kt = t((() => {
	_e(), p(), v(), i(), we(), g(), Ue(), D.on("copilot-project-compilation-error", (e) => {
		if (e.detail.error) {
			let t;
			if (e.detail.files && e.detail.files.length > 0) {
				let n = O.idePluginState?.supportedActions?.includes("undo") ? b`
            <vaadin-button
              @click="${(t) => {
					t.preventDefault(), D.emit("undoRedo", {
						undo: !0,
						files: e.detail.files?.map((e) => e.path)
					});
				}}"
              theme="primary">
              <vaadin-icon slot="prefix" .svg="${y.undo}"></vaadin-icon>
              Undo Last Change
            </vaadin-button>
          ` : _;
				t = ne(b`
        <span> Following files have compilation errors: </span>
        <ul class="list-none mb-0 mt-2 p-0">
          ${e.detail.files.map((e) => b` <li>
                <vaadin-button
                  @click="${() => {
					D.emit("show-in-ide", { javaSource: { absoluteFilePath: e.path } });
				}}"
                  theme="tertiary">
                  <vaadin-icon slot="prefix" .svg="${y.code}"></vaadin-icon>
                  ${e.name}
                </vaadin-button>
              </li>`)}
        </ul>
        <hr class="border-b border-e-0 border-s-0 border-t-0 mb-3 mt-2 mx-0 w-full" />
        ${n}
      `);
			} else t = "Project contains one or more compilation errors.";
			Gt = We({
				message: "Compilation error",
				details: t,
				type: r.WARNING,
				delay: 3e4
			});
		} else Gt && Ge(Gt), Gt = void 0;
	});
})), qt, Jt, Yt = t((() => {
	v(), me(), qt = /* @__PURE__ */ e(rt(), 1), g(), fe(), Jt = class extends h {
		constructor(...e) {
			super(...e), this.text = () => (this.parentElement.textContent ?? "").trim();
		}
		createRenderRoot() {
			return this;
		}
		render() {
			return b`<vaadin-button
      aria-label="Copy to clipboard"
      @click=${(e) => {
				e.stopPropagation(), e.preventDefault(), (0, qt.default)(this.text());
			}}
      theme="icon tertiary">
      <vaadin-icon .svg="${y.fileCopy}"></vaadin-icon>
      <vaadin-tooltip slot="tooltip" text="Copy to clipboard"></vaadin-tooltip>
    </vaadin-button> `;
		}
	}, w([ge({ type: Function })], Jt.prototype, "text", void 0), Jt = w([ue("copilot-copy")], Jt);
})), Xt, Zt = t((() => {
	Xt = {
		ACCEPTED: 202,
		BAD_GATEWAY: 502,
		BAD_REQUEST: 400,
		CONFLICT: 409,
		CONTINUE: 100,
		CREATED: 201,
		EXPECTATION_FAILED: 417,
		FORBIDDEN: 403,
		GATEWAY_TIMEOUT: 504,
		GONE: 410,
		HTTP_VERSION_NOT_SUPPORTED: 505,
		IM_A_TEAPOT: 418,
		INSUFFICIENT_SPACE_ON_RESOURCE: 419,
		INSUFFICIENT_STORAGE: 507,
		INTERNAL_SERVER_ERROR: 500,
		LENGTH_REQUIRED: 411,
		LOCKED: 423,
		METHOD_FAILURE: 420,
		METHOD_NOT_ALLOWED: 405,
		MOVED_PERMANENTLY: 301,
		MOVED_TEMPORARILY: 302,
		MULTI_STATUS: 207,
		MULTIPLE_CHOICES: 300,
		NETWORK_AUTHENTICATION_REQUIRED: 511,
		NO_CONTENT: 204,
		NON_AUTHORITATIVE_INFORMATION: 203,
		NOT_ACCEPTABLE: 406,
		NOT_FOUND: 404,
		NOT_IMPLEMENTED: 501,
		NOT_MODIFIED: 304,
		OK: 200,
		PARTIAL_CONTENT: 206,
		PAYMENT_REQUIRED: 402,
		PERMANENT_REDIRECT: 308,
		PRECONDITION_FAILED: 412,
		PRECONDITION_REQUIRED: 428,
		PROCESSING: 102,
		PROXY_AUTHENTICATION_REQUIRED: 407,
		REQUEST_HEADER_FIELDS_TOO_LARGE: 431,
		REQUEST_TIMEOUT: 408,
		REQUEST_TOO_LONG: 413,
		REQUEST_URI_TOO_LONG: 414,
		REQUESTED_RANGE_NOT_SATISFIABLE: 416,
		RESET_CONTENT: 205,
		SEE_OTHER: 303,
		SERVICE_UNAVAILABLE: 503,
		SWITCHING_PROTOCOLS: 101,
		TEMPORARY_REDIRECT: 307,
		TOO_MANY_REQUESTS: 429,
		UNAUTHORIZED: 401,
		UNPROCESSABLE_ENTITY: 422,
		UNSUPPORTED_MEDIA_TYPE: 415,
		USE_PROXY: 305
	};
})), Qt, $t, en = t((() => {
	Qt = {
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
	}, $t = {
		Accepted: 202,
		"Bad Gateway": 502,
		"Bad Request": 400,
		Conflict: 409,
		Continue: 100,
		Created: 201,
		"Expectation Failed": 417,
		"Failed Dependency": 424,
		Forbidden: 403,
		"Gateway Timeout": 504,
		Gone: 410,
		"HTTP Version Not Supported": 505,
		"I'm a teapot": 418,
		"Insufficient Space on Resource": 419,
		"Insufficient Storage": 507,
		"Internal Server Error": 500,
		"Length Required": 411,
		Locked: 423,
		"Method Failure": 420,
		"Method Not Allowed": 405,
		"Moved Permanently": 301,
		"Moved Temporarily": 302,
		"Multi-Status": 207,
		"Multiple Choices": 300,
		"Network Authentication Required": 511,
		"No Content": 204,
		"Non Authoritative Information": 203,
		"Not Acceptable": 406,
		"Not Found": 404,
		"Not Implemented": 501,
		"Not Modified": 304,
		OK: 200,
		"Partial Content": 206,
		"Payment Required": 402,
		"Permanent Redirect": 308,
		"Precondition Failed": 412,
		"Precondition Required": 428,
		Processing: 102,
		"Early Hints": 103,
		"Upgrade Required": 426,
		"Proxy Authentication Required": 407,
		"Request Header Fields Too Large": 431,
		"Request Timeout": 408,
		"Request Entity Too Large": 413,
		"Request-URI Too Long": 414,
		"Requested Range Not Satisfiable": 416,
		"Reset Content": 205,
		"See Other": 303,
		"Service Unavailable": 503,
		"Switching Protocols": 101,
		"Temporary Redirect": 307,
		"Too Many Requests": 429,
		Unauthorized: 401,
		"Unavailable For Legal Reasons": 451,
		"Unprocessable Entity": 422,
		"Unsupported Media Type": 415,
		"Use Proxy": 305,
		"Misdirected Request": 421
	};
}));
//#endregion
//#region node_modules/http-status-codes/build/es/utils-functions.js
function tn(e) {
	var t = Qt[e.toString()];
	if (!t) throw Error("Status code does not exist: " + e);
	return t;
}
function nn(e) {
	var t = $t[e];
	if (!t) throw Error("Reason phrase does not exist: " + e);
	return t;
}
var rn, an = t((() => {
	en(), rn = tn;
})), on, sn = t((() => {
	Zt(), an(), Zt(), on = function() {
		return on = Object.assign || function(e) {
			for (var t, n = 1, r = arguments.length; n < r; n++) for (var i in t = arguments[n], t) Object.prototype.hasOwnProperty.call(t, i) && (e[i] = t[i]);
			return e;
		}, on.apply(this, arguments);
	}, on(on({}, Xt), {
		getStatusCode: nn,
		getStatusText: rn
	});
}));
//#endregion
//#region frontend/copilot/endpoint-logger.ts
function cn(e) {
	return `endpoint-request-${e.id}`;
}
function ln(e) {
	return typeof e == "string" ? `${e}` : JSON.stringify(e, void 0, 2);
}
var un = t((() => {
	_e(), p(), i(), v(), Yt(), sn(), D.on("endpoint-request", (e) => {
		let t = e.detail, n = cn(t);
		delete t.id;
		let i = Object.values(t.params), a = i.map(ln).join(", ");
		D.emit("log", {
			id: n,
			type: r.INFORMATION,
			message: `Called endpoint ${t.endpoint}.${t.method}(${a})`,
			expandedMessage: ne(b`Called endpoint ${t.endpoint}.${t.method} with parameters
        <code class="codeblock"><copilot-copy></copilot-copy>${ln(i)}</code>`),
			details: "Response: <pending>"
		});
	}), D.on("endpoint-response", (e) => {
		let t;
		try {
			t = JSON.parse(e.detail.text);
		} catch {
			t = e.detail.text;
		}
		let n = {}, i = e.detail.status ?? 200;
		i === 200 ? (n.details = `Response: ${ln(t)}`, n.expandedDetails = ne(b`Response: <code class="codeblock"><copilot-copy></copilot-copy>${ln(t)}</code>`)) : (n.details = `Error: ${i} ${tn(i)}`, n.type = r.ERROR), D.emit("update-log", {
			id: cn(e.detail),
			...n
		});
	});
})), dn, fn, A, pn = t((() => {
	v(), me(), g(), _e(), Le(), Fe(), i(), fe(), fn = class extends CustomEvent {
		constructor(e) {
			super("show-in-ide-clicked", {
				detail: e,
				bubbles: !0,
				composed: !0
			});
		}
	}, A = (dn = class extends h {
		constructor(...e) {
			super(...e), this.iconHidden = !1, this.linkHidden = !1, this.tooltipText = void 0, this.linkText = void 0, this.source = void 0, this.javaSource = void 0, this.node = void 0;
		}
		static get styles() {
			return [m(je), m(ke)];
		}
		connectedCallback() {
			super.connectedCallback(), this.classList.add("contents");
		}
		render() {
			return this.iconHidden ? this.linkHidden ? _ : this.renderContent(this.renderAnchor()) : this.linkHidden ? this.renderContent(this.renderIconButton()) : this.renderContent(this.renderAnchorWithIcon());
		}
		renderContent(e) {
			return b` <div class="contents">${e}</div> `;
		}
		renderIconButton() {
			let e = this.tooltipText ?? `Open ${this.getFileName()} in IDE`;
			return b`
      <vaadin-button
        aria-label="${e}"
        id="show-in-ide"
        theme="icon tertiary"
        @click=${(e) => {
				e.stopPropagation(), e.preventDefault(), this._showInIde();
			}}>
        <vaadin-icon .svg="${y.code}"></vaadin-icon>
        <vaadin-tooltip slot="tooltip" text="${e}"></vaadin-tooltip>
      </vaadin-button>
    `;
		}
		renderAnchorWithIcon() {
			return b`
      <a
        class="gap-1.5 inline-flex py-1.5 text-blue-11"
        href="#"
        id="link"
        @click=${(e) => {
				e.preventDefault(), this._showInIde();
			}}>
        <vaadin-icon .svg="${y.fileOpen}"></vaadin-icon>
        ${this.linkText ?? this.getFileName() ?? ""}
      </a>
      ${this.renderTooltip("link")}
    `;
		}
		renderAnchor() {
			return b`
      <a
        class="text-blue-11"
        href="#"
        id="link"
        @click=${(e) => {
				e.preventDefault(), this._showInIde();
			}}
        >${this.linkText ?? this.getFileName() ?? ""}</a
      >
      ${this.renderTooltip("link")}
    `;
		}
		dispatchClickedEvent() {
			this.dispatchEvent(new fn({
				source: this.source,
				javaSource: this.javaSource,
				node: this.node
			}));
		}
		renderTooltip(e) {
			return b`<vaadin-tooltip for="${e}" text="${this.tooltipText ?? `Open ${this.getFileName()} in IDE`}" position="top-start"></vaadin-tooltip>`;
		}
		getFileName() {
			if (this.tooltipText) return this.tooltipText;
			if (this.source && this.source.fileName) return te(this.source.fileName);
			if (this.javaSource) return this.javaSource.className;
		}
		_showInIde() {
			D.emit("show-in-ide", {
				source: this.source,
				javaSource: this.javaSource,
				node: this.node
			}), this.dispatchClickedEvent();
		}
	}, dn.TAG = "copilot-go-to-source", dn), w([ge({ type: Boolean })], A.prototype, "iconHidden", void 0), w([ge({ type: Boolean })], A.prototype, "linkHidden", void 0), w([ge()], A.prototype, "tooltipText", void 0), w([ge()], A.prototype, "linkText", void 0), w([ge()], A.prototype, "source", void 0), w([ge()], A.prototype, "javaSource", void 0), w([ge()], A.prototype, "node", void 0), A = w([ue(A.TAG)], A);
}));
//#endregion
//#region node_modules/sortablejs/modular/sortable.esm.js
function mn(e, t) {
	var n = Object.keys(e);
	if (Object.getOwnPropertySymbols) {
		var r = Object.getOwnPropertySymbols(e);
		t && (r = r.filter(function(t) {
			return Object.getOwnPropertyDescriptor(e, t).enumerable;
		})), n.push.apply(n, r);
	}
	return n;
}
function hn(e) {
	for (var t = 1; t < arguments.length; t++) {
		var n = arguments[t] == null ? {} : arguments[t];
		t % 2 ? mn(Object(n), !0).forEach(function(t) {
			_n(e, t, n[t]);
		}) : Object.getOwnPropertyDescriptors ? Object.defineProperties(e, Object.getOwnPropertyDescriptors(n)) : mn(Object(n)).forEach(function(t) {
			Object.defineProperty(e, t, Object.getOwnPropertyDescriptor(n, t));
		});
	}
	return e;
}
function gn(e) {
	"@babel/helpers - typeof";
	return gn = typeof Symbol == "function" && typeof Symbol.iterator == "symbol" ? function(e) {
		return typeof e;
	} : function(e) {
		return e && typeof Symbol == "function" && e.constructor === Symbol && e !== Symbol.prototype ? "symbol" : typeof e;
	}, gn(e);
}
function _n(e, t, n) {
	return t in e ? Object.defineProperty(e, t, {
		value: n,
		enumerable: !0,
		configurable: !0,
		writable: !0
	}) : e[t] = n, e;
}
function vn() {
	return vn = Object.assign || function(e) {
		for (var t = 1; t < arguments.length; t++) {
			var n = arguments[t];
			for (var r in n) Object.prototype.hasOwnProperty.call(n, r) && (e[r] = n[r]);
		}
		return e;
	}, vn.apply(this, arguments);
}
function yn(e, t) {
	if (e == null) return {};
	var n = {}, r = Object.keys(e), i, a;
	for (a = 0; a < r.length; a++) i = r[a], !(t.indexOf(i) >= 0) && (n[i] = e[i]);
	return n;
}
function bn(e, t) {
	if (e == null) return {};
	var n = yn(e, t), r, i;
	if (Object.getOwnPropertySymbols) {
		var a = Object.getOwnPropertySymbols(e);
		for (i = 0; i < a.length; i++) r = a[i], !(t.indexOf(r) >= 0) && Object.prototype.propertyIsEnumerable.call(e, r) && (n[r] = e[r]);
	}
	return n;
}
function xn(e) {
	if (typeof window < "u" && window.navigator) return !!/* @__PURE__ */ navigator.userAgent.match(e);
}
function j(e, t, n) {
	e.addEventListener(t, n, !cr && mr);
}
function M(e, t, n) {
	e.removeEventListener(t, n, !cr && mr);
}
function Sn(e, t) {
	if (t) {
		if (t[0] === ">" && (t = t.substring(1)), e) try {
			if (e.matches) return e.matches(t);
			if (e.msMatchesSelector) return e.msMatchesSelector(t);
			if (e.webkitMatchesSelector) return e.webkitMatchesSelector(t);
		} catch {
			return !1;
		}
		return !1;
	}
}
function Cn(e) {
	return e.host && e !== document && e.host.nodeType ? e.host : e.parentNode;
}
function N(e, t, n, r) {
	if (e) {
		n ||= document;
		do {
			if (t != null && (t[0] === ">" ? e.parentNode === n && Sn(e, t) : Sn(e, t)) || r && e === n) return e;
			if (e === n) break;
		} while (e = Cn(e));
	}
	return null;
}
function P(e, t, n) {
	e && t && (e.classList ? e.classList[n ? "add" : "remove"](t) : e.className = ((" " + e.className + " ").replace(hr, " ").replace(" " + t + " ", " ") + (n ? " " + t : "")).replace(hr, " "));
}
function F(e, t, n) {
	var r = e && e.style;
	if (r) {
		if (n === void 0) return document.defaultView && document.defaultView.getComputedStyle ? n = document.defaultView.getComputedStyle(e, "") : e.currentStyle && (n = e.currentStyle), t === void 0 ? n : n[t];
		!(t in r) && t.indexOf("webkit") === -1 && (t = "-webkit-" + t), r[t] = n + (typeof n == "string" ? "" : "px");
	}
}
function wn(e, t) {
	var n = "";
	if (typeof e == "string") n = e;
	else do {
		var r = F(e, "transform");
		r && r !== "none" && (n = r + " " + n);
	} while (!t && (e = e.parentNode));
	var i = window.DOMMatrix || window.WebKitCSSMatrix || window.CSSMatrix || window.MSCSSMatrix;
	return i && new i(n);
}
function Tn(e, t, n) {
	if (e) {
		var r = e.getElementsByTagName(t), i = 0, a = r.length;
		if (n) for (; i < a; i++) n(r[i], i);
		return r;
	}
	return [];
}
function En() {
	return document.scrollingElement || document.documentElement;
}
function I(e, t, n, r, i) {
	if (!(!e.getBoundingClientRect && e !== window)) {
		var a, o, s, c, l, u, d;
		if (e !== window && e.parentNode && e !== En() ? (a = e.getBoundingClientRect(), o = a.top, s = a.left, c = a.bottom, l = a.right, u = a.height, d = a.width) : (o = 0, s = 0, c = window.innerHeight, l = window.innerWidth, u = window.innerHeight, d = window.innerWidth), (t || n) && e !== window && (i ||= e.parentNode, !cr)) do
			if (i && i.getBoundingClientRect && (F(i, "transform") !== "none" || n && F(i, "position") !== "static")) {
				var f = i.getBoundingClientRect();
				o -= f.top + parseInt(F(i, "border-top-width")), s -= f.left + parseInt(F(i, "border-left-width")), c = o + a.height, l = s + a.width;
				break;
			}
		while (i = i.parentNode);
		if (r && e !== window) {
			var p = wn(i || e), m = p && p.a, h = p && p.d;
			p && (o /= h, s /= m, d /= m, u /= h, c = o + u, l = s + d);
		}
		return {
			top: o,
			left: s,
			bottom: c,
			right: l,
			width: d,
			height: u
		};
	}
}
function Dn(e, t, n) {
	for (var r = Mn(e, !0), i = I(e)[t]; r;) {
		var a = I(r)[n], o = void 0;
		if (o = n === "top" || n === "left" ? i >= a : i <= a, !o) return r;
		if (r === En()) break;
		r = Mn(r, !1);
	}
	return !1;
}
function On(e, t, n, r) {
	for (var i = 0, a = 0, o = e.children; a < o.length;) {
		if (o[a].style.display !== "none" && o[a] !== z.ghost && (r || o[a] !== z.dragged) && N(o[a], n.draggable, e, !1)) {
			if (i === t) return o[a];
			i++;
		}
		a++;
	}
	return null;
}
function kn(e, t) {
	for (var n = e.lastElementChild; n && (n === z.ghost || F(n, "display") === "none" || t && !Sn(n, t));) n = n.previousElementSibling;
	return n || null;
}
function L(e, t) {
	var n = 0;
	if (!e || !e.parentNode) return -1;
	for (; e = e.previousElementSibling;) e.nodeName.toUpperCase() !== "TEMPLATE" && e !== z.clone && (!t || Sn(e, t)) && n++;
	return n;
}
function An(e) {
	var t = 0, n = 0, r = En();
	if (e) do {
		var i = wn(e), a = i.a, o = i.d;
		t += e.scrollLeft * a, n += e.scrollTop * o;
	} while (e !== r && (e = e.parentNode));
	return [t, n];
}
function jn(e, t) {
	for (var n in e) if (e.hasOwnProperty(n)) {
		for (var r in t) if (t.hasOwnProperty(r) && t[r] === e[n][r]) return Number(n);
	}
	return -1;
}
function Mn(e, t) {
	if (!e || !e.getBoundingClientRect) return En();
	var n = e, r = !1;
	do
		if (n.clientWidth < n.scrollWidth || n.clientHeight < n.scrollHeight) {
			var i = F(n);
			if (n.clientWidth < n.scrollWidth && (i.overflowX == "auto" || i.overflowX == "scroll") || n.clientHeight < n.scrollHeight && (i.overflowY == "auto" || i.overflowY == "scroll")) {
				if (!n.getBoundingClientRect || n === document.body) return En();
				if (r || t) return n;
				r = !0;
			}
		}
	while (n = n.parentNode);
	return En();
}
function Nn(e, t) {
	if (e && t) for (var n in t) t.hasOwnProperty(n) && (e[n] = t[n]);
	return e;
}
function Pn(e, t) {
	return Math.round(e.top) === Math.round(t.top) && Math.round(e.left) === Math.round(t.left) && Math.round(e.height) === Math.round(t.height) && Math.round(e.width) === Math.round(t.width);
}
function Fn(e, t) {
	return function() {
		if (!gr) {
			var n = arguments, r = this;
			n.length === 1 ? e.call(r, n[0]) : e.apply(r, n), gr = setTimeout(function() {
				gr = void 0;
			}, t);
		}
	};
}
function In() {
	clearTimeout(gr), gr = void 0;
}
function Ln(e, t, n) {
	e.scrollLeft += t, e.scrollTop += n;
}
function Rn(e) {
	var t = window.Polymer, n = window.jQuery || window.Zepto;
	return t && t.dom ? t.dom(e).cloneNode(!0) : n ? n(e).clone(!0)[0] : e.cloneNode(!0);
}
function zn(e, t, n) {
	var r = {};
	return Array.from(e.children).forEach(function(i) {
		if (!(!N(i, t.draggable, e, !1) || i.animated || i === n)) {
			var a = I(i);
			r.left = Math.min(r.left ?? Infinity, a.left), r.top = Math.min(r.top ?? Infinity, a.top), r.right = Math.max(r.right ?? -Infinity, a.right), r.bottom = Math.max(r.bottom ?? -Infinity, a.bottom);
		}
	}), r.width = r.right - r.left, r.height = r.bottom - r.top, r.x = r.left, r.y = r.top, r;
}
function Bn() {
	var e = [], t;
	return {
		captureAnimationState: function() {
			e = [], this.options.animation && [].slice.call(this.el.children).forEach(function(t) {
				if (!(F(t, "display") === "none" || t === z.ghost)) {
					e.push({
						target: t,
						rect: I(t)
					});
					var n = hn({}, e[e.length - 1].rect);
					if (t.thisAnimationDuration) {
						var r = wn(t, !0);
						r && (n.top -= r.f, n.left -= r.e);
					}
					t.fromRect = n;
				}
			});
		},
		addAnimationState: function(t) {
			e.push(t);
		},
		removeAnimationState: function(t) {
			e.splice(jn(e, { target: t }), 1);
		},
		animateAll: function(n) {
			var r = this;
			if (!this.options.animation) {
				clearTimeout(t), typeof n == "function" && n();
				return;
			}
			var i = !1, a = 0;
			e.forEach(function(e) {
				var t = 0, n = e.target, o = n.fromRect, s = I(n), c = n.prevFromRect, l = n.prevToRect, u = e.rect, d = wn(n, !0);
				d && (s.top -= d.f, s.left -= d.e), n.toRect = s, n.thisAnimationDuration && Pn(c, s) && !Pn(o, s) && (u.top - s.top) / (u.left - s.left) === (o.top - s.top) / (o.left - s.left) && (t = Hn(u, c, l, r.options)), Pn(s, o) || (n.prevFromRect = o, n.prevToRect = s, t ||= r.options.animation, r.animate(n, u, s, t)), t && (i = !0, a = Math.max(a, t), clearTimeout(n.animationResetTimer), n.animationResetTimer = setTimeout(function() {
					n.animationTime = 0, n.prevFromRect = null, n.fromRect = null, n.prevToRect = null, n.thisAnimationDuration = null;
				}, t), n.thisAnimationDuration = t);
			}), clearTimeout(t), i ? t = setTimeout(function() {
				typeof n == "function" && n();
			}, a) : typeof n == "function" && n(), e = [];
		},
		animate: function(e, t, n, r) {
			if (r) {
				F(e, "transition", ""), F(e, "transform", "");
				var i = wn(this.el), a = i && i.a, o = i && i.d, s = (t.left - n.left) / (a || 1), c = (t.top - n.top) / (o || 1);
				e.animatingX = !!s, e.animatingY = !!c, F(e, "transform", "translate3d(" + s + "px," + c + "px,0)"), this.forRepaintDummy = Vn(e), F(e, "transition", "transform " + r + "ms" + (this.options.easing ? " " + this.options.easing : "")), F(e, "transform", "translate3d(0,0,0)"), typeof e.animated == "number" && clearTimeout(e.animated), e.animated = setTimeout(function() {
					F(e, "transition", ""), F(e, "transform", ""), e.animated = !1, e.animatingX = !1, e.animatingY = !1;
				}, r);
			}
		}
	};
}
function Vn(e) {
	return e.offsetWidth;
}
function Hn(e, t, n, r) {
	return Math.sqrt((t.top - e.top) ** 2 + (t.left - e.left) ** 2) / Math.sqrt((t.top - n.top) ** 2 + (t.left - n.left) ** 2) * r.animation;
}
function Un(e) {
	var t = e.sortable, n = e.rootEl, r = e.name, i = e.targetEl, a = e.cloneEl, o = e.toEl, s = e.fromEl, c = e.oldIndex, l = e.newIndex, u = e.oldDraggableIndex, d = e.newDraggableIndex, f = e.originalEvent, p = e.putSortable, m = e.extraEventProperties;
	if (t ||= n && n[B], t) {
		var h, g = t.options, _ = "on" + r.charAt(0).toUpperCase() + r.substr(1);
		window.CustomEvent && !cr && !lr ? h = new CustomEvent(r, {
			bubbles: !0,
			cancelable: !0
		}) : (h = document.createEvent("Event"), h.initEvent(r, !0, !0)), h.to = o || n, h.from = s || n, h.item = i || n, h.clone = a, h.oldIndex = c, h.newIndex = l, h.oldDraggableIndex = u, h.newDraggableIndex = d, h.originalEvent = f, h.pullMode = p ? p.lastPutMode : void 0;
		var v = hn(hn({}, m), yr.getEventProperties(r, t));
		for (var y in v) h[y] = v[y];
		n && n.dispatchEvent(h), g[_] && g[_].call(t, h);
	}
}
function R(e) {
	Un(hn({
		putSortable: J,
		cloneEl: K,
		targetEl: H,
		rootEl: G,
		oldIndex: wr,
		oldDraggableIndex: Tr,
		newIndex: q,
		newDraggableIndex: Er
	}, e));
}
function z(e, t) {
	if (!(e && e.nodeType && e.nodeType === 1)) throw `Sortable: \`el\` must be an HTMLElement, not ${{}.toString.call(e)}`;
	this.el = e, this.options = t = vn({}, t), e[B] = this;
	var n = {
		group: null,
		sort: !0,
		disabled: !1,
		store: null,
		handle: null,
		draggable: /^[uo]l$/i.test(e.nodeName) ? ">li" : ">*",
		swapThreshold: 1,
		invertSwap: !1,
		invertedSwapThreshold: null,
		removeCloneOnHide: !0,
		direction: function() {
			return Xr(e, this.options);
		},
		ghostClass: "sortable-ghost",
		chosenClass: "sortable-chosen",
		dragClass: "sortable-drag",
		ignore: "a, img",
		filter: null,
		preventOnFilter: !0,
		animation: 0,
		easing: null,
		setData: function(e, t) {
			e.setData("Text", t.textContent);
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
		supportPointer: z.supportPointer !== !1 && "PointerEvent" in window && (!dr || fr),
		emptyInsertThreshold: 5
	};
	for (var r in yr.initializePlugins(this, e, n), n) !(r in t) && (t[r] = n[r]);
	for (var i in $r(t), this) i.charAt(0) === "_" && typeof this[i] == "function" && (this[i] = this[i].bind(this));
	this.nativeDraggable = t.forceFallback ? !1 : Jr, this.nativeDraggable && (this.options.touchStartThreshold = 1), t.supportPointer ? j(e, "pointerdown", this._onTapStart) : (j(e, "mousedown", this._onTapStart), j(e, "touchstart", this._onTapStart)), this.nativeDraggable && (j(e, "dragover", this), j(e, "dragenter", this)), Ar.push(this.el), t.store && t.store.get && this.sort(t.store.get(this) || []), vn(this, Bn());
}
function Wn(e) {
	e.dataTransfer && (e.dataTransfer.dropEffect = "move"), e.cancelable && e.preventDefault();
}
function Gn(e, t, n, r, i, a, o, s) {
	var c, l = e[B], u = l.options.onMove, d;
	return window.CustomEvent && !cr && !lr ? c = new CustomEvent("move", {
		bubbles: !0,
		cancelable: !0
	}) : (c = document.createEvent("Event"), c.initEvent("move", !0, !0)), c.to = t, c.from = e, c.dragged = n, c.draggedRect = r, c.related = i || t, c.relatedRect = a || I(t), c.willInsertAfter = s, c.originalEvent = o, e.dispatchEvent(c), u && (d = u.call(l, c, o)), d;
}
function Kn(e) {
	e.draggable = !1;
}
function qn() {
	Ur = !1;
}
function Jn(e, t, n) {
	var r = I(On(n.el, 0, n.options, !0)), i = zn(n.el, n.options, W), a = 10;
	return t ? e.clientX < i.left - a || e.clientY < r.top && e.clientX < r.right : e.clientY < i.top - a || e.clientY < r.bottom && e.clientX < r.left;
}
function Yn(e, t, n) {
	var r = I(kn(n.el, n.options.draggable)), i = zn(n.el, n.options, W), a = 10;
	return t ? e.clientX > i.right + a || e.clientY > r.bottom && e.clientX > r.left : e.clientY > i.bottom + a || e.clientX > r.right && e.clientY > r.top;
}
function Xn(e, t, n, r, i, a, o, s) {
	var c = r ? e.clientY : e.clientX, l = r ? n.height : n.width, u = r ? n.top : n.left, d = r ? n.bottom : n.right, f = !1;
	if (!o) {
		if (s && Vr < l * i) {
			if (!zr && (Rr === 1 ? c > u + l * a / 2 : c < d - l * a / 2) && (zr = !0), zr) f = !0;
			else if (Rr === 1 ? c < u + Vr : c > d - Vr) return -Rr;
		} else if (c > u + l * (1 - i) / 2 && c < d - l * (1 - i) / 2) return Zn(t);
	}
	return f ||= o, f && (c < u + l * a / 2 || c > d - l * a / 2) ? c > u + l / 2 ? 1 : -1 : 0;
}
function Zn(e) {
	return L(H) < L(e) ? 1 : -1;
}
function Qn(e) {
	for (var t = e.tagName + e.className + e.src + e.href + e.textContent, n = t.length, r = 0; n--;) r += t.charCodeAt(n);
	return r.toString(36);
}
function $n(e) {
	Wr.length = 0;
	for (var t = e.getElementsByTagName("input"), n = t.length; n--;) {
		var r = t[n];
		r.checked && Wr.push(r);
	}
}
function er(e) {
	return setTimeout(e, 0);
}
function tr(e) {
	return clearTimeout(e);
}
function nr() {
	function e() {
		for (var e in this.defaults = {
			scroll: !0,
			forceAutoScrollFallback: !1,
			scrollSensitivity: 30,
			scrollSpeed: 10,
			bubbleScroll: !0
		}, this) e.charAt(0) === "_" && typeof this[e] == "function" && (this[e] = this[e].bind(this));
	}
	return e.prototype = {
		dragStarted: function(e) {
			var t = e.originalEvent;
			this.sortable.nativeDraggable ? j(document, "dragover", this._handleAutoScroll) : this.options.supportPointer ? j(document, "pointermove", this._handleFallbackAutoScroll) : t.touches ? j(document, "touchmove", this._handleFallbackAutoScroll) : j(document, "mousemove", this._handleFallbackAutoScroll);
		},
		dragOverCompleted: function(e) {
			var t = e.originalEvent;
			!this.options.dragOverBubble && !t.rootEl && this._handleAutoScroll(t);
		},
		drop: function() {
			this.sortable.nativeDraggable ? M(document, "dragover", this._handleAutoScroll) : (M(document, "pointermove", this._handleFallbackAutoScroll), M(document, "touchmove", this._handleFallbackAutoScroll), M(document, "mousemove", this._handleFallbackAutoScroll)), ir(), rr(), In();
		},
		nulling: function() {
			li = ai = ii = oi = ui = si = ci = null, Z.length = 0;
		},
		_handleFallbackAutoScroll: function(e) {
			this._handleAutoScroll(e, !0);
		},
		_handleAutoScroll: function(e, t) {
			var n = this, r = (e.touches ? e.touches[0] : e).clientX, i = (e.touches ? e.touches[0] : e).clientY, a = document.elementFromPoint(r, i);
			if (li = e, t || this.options.forceAutoScrollFallback || lr || cr || dr) {
				di(e, this.options, a, t);
				var o = Mn(a, !0);
				oi && (!ui || r !== si || i !== ci) && (ui && ir(), ui = setInterval(function() {
					var a = Mn(document.elementFromPoint(r, i), !0);
					a !== o && (o = a, rr()), di(e, n.options, a, t);
				}, 10), si = r, ci = i);
			} else {
				if (!this.options.bubbleScroll || Mn(a, !0) === En()) {
					rr();
					return;
				}
				di(e, this.options, Mn(a, !1), !1);
			}
		}
	}, vn(e, {
		pluginName: "scroll",
		initializeByDefault: !0
	});
}
function rr() {
	Z.forEach(function(e) {
		clearInterval(e.pid);
	}), Z = [];
}
function ir() {
	clearInterval(ui);
}
function ar() {}
function or() {}
var sr, cr, lr, ur, dr, fr, pr, mr, hr, gr, B, _r, vr, yr, br, V, H, U, W, G, xr, Sr, K, Cr, wr, q, Tr, Er, Dr, J, Or, kr, Ar, jr, Y, Mr, Nr, Pr, Fr, Ir, Lr, Rr, zr, Br, Vr, X, Hr, Ur, Wr, Gr, Kr, qr, Jr, Yr, Xr, Zr, Qr, $r, ei, ti, ni, ri, Z, ii, ai, oi, si, ci, li, ui, di, fi, pi = t((() => {
	sr = "1.15.6", cr = xn(/(?:Trident.*rv[ :]?11\.|msie|iemobile|Windows Phone)/i), lr = xn(/Edge/i), ur = xn(/firefox/i), dr = xn(/safari/i) && !xn(/chrome/i) && !xn(/android/i), fr = xn(/iP(ad|od|hone)/i), pr = xn(/chrome/i) && xn(/android/i), mr = {
		capture: !1,
		passive: !1
	}, hr = /\s+/g, B = "Sortable" + (/* @__PURE__ */ new Date()).getTime(), _r = [], vr = { initializeByDefault: !0 }, yr = {
		mount: function(e) {
			for (var t in vr) vr.hasOwnProperty(t) && !(t in e) && (e[t] = vr[t]);
			_r.forEach(function(t) {
				if (t.pluginName === e.pluginName) throw `Sortable: Cannot mount plugin ${e.pluginName} more than once`;
			}), _r.push(e);
		},
		pluginEvent: function(e, t, n) {
			var r = this;
			this.eventCanceled = !1, n.cancel = function() {
				r.eventCanceled = !0;
			};
			var i = e + "Global";
			_r.forEach(function(r) {
				t[r.pluginName] && (t[r.pluginName][i] && t[r.pluginName][i](hn({ sortable: t }, n)), t.options[r.pluginName] && t[r.pluginName][e] && t[r.pluginName][e](hn({ sortable: t }, n)));
			});
		},
		initializePlugins: function(e, t, n, r) {
			for (var i in _r.forEach(function(r) {
				var i = r.pluginName;
				if (!(!e.options[i] && !r.initializeByDefault)) {
					var a = new r(e, t, e.options);
					a.sortable = e, a.options = e.options, e[i] = a, vn(n, a.defaults);
				}
			}), e.options) if (e.options.hasOwnProperty(i)) {
				var a = this.modifyOption(e, i, e.options[i]);
				a !== void 0 && (e.options[i] = a);
			}
		},
		getEventProperties: function(e, t) {
			var n = {};
			return _r.forEach(function(r) {
				typeof r.eventProperties == "function" && vn(n, r.eventProperties.call(t[r.pluginName], e));
			}), n;
		},
		modifyOption: function(e, t, n) {
			var r;
			return _r.forEach(function(i) {
				e[i.pluginName] && i.optionListeners && typeof i.optionListeners[t] == "function" && (r = i.optionListeners[t].call(e[i.pluginName], n));
			}), r;
		}
	}, br = ["evt"], V = function(e, t) {
		var n = arguments.length > 2 && arguments[2] !== void 0 ? arguments[2] : {}, r = n.evt, i = bn(n, br);
		yr.pluginEvent.bind(z)(e, t, hn({
			dragEl: H,
			parentEl: U,
			ghostEl: W,
			rootEl: G,
			nextEl: xr,
			lastDownEl: Sr,
			cloneEl: K,
			cloneHidden: Cr,
			dragStarted: Ir,
			putSortable: J,
			activeSortable: z.active,
			originalEvent: r,
			oldIndex: wr,
			oldDraggableIndex: Tr,
			newIndex: q,
			newDraggableIndex: Er,
			hideGhostForTarget: ei,
			unhideGhostForTarget: ti,
			cloneNowHidden: function() {
				Cr = !0;
			},
			cloneNowShown: function() {
				Cr = !1;
			},
			dispatchSortableEvent: function(e) {
				R({
					sortable: t,
					name: e,
					originalEvent: r
				});
			}
		}, i));
	}, Or = !1, kr = !1, Ar = [], zr = !1, Br = !1, Hr = [], Ur = !1, Wr = [], Gr = typeof document < "u", Kr = fr, qr = lr || cr ? "cssFloat" : "float", Jr = Gr && !pr && !fr && "draggable" in document.createElement("div"), Yr = function() {
		if (Gr) {
			if (cr) return !1;
			var e = document.createElement("x");
			return e.style.cssText = "pointer-events:auto", e.style.pointerEvents === "auto";
		}
	}(), Xr = function(e, t) {
		var n = F(e), r = parseInt(n.width) - parseInt(n.paddingLeft) - parseInt(n.paddingRight) - parseInt(n.borderLeftWidth) - parseInt(n.borderRightWidth), i = On(e, 0, t), a = On(e, 1, t), o = i && F(i), s = a && F(a), c = o && parseInt(o.marginLeft) + parseInt(o.marginRight) + I(i).width, l = s && parseInt(s.marginLeft) + parseInt(s.marginRight) + I(a).width;
		if (n.display === "flex") return n.flexDirection === "column" || n.flexDirection === "column-reverse" ? "vertical" : "horizontal";
		if (n.display === "grid") return n.gridTemplateColumns.split(" ").length <= 1 ? "vertical" : "horizontal";
		if (i && o.float && o.float !== "none") {
			var u = o.float === "left" ? "left" : "right";
			return a && (s.clear === "both" || s.clear === u) ? "vertical" : "horizontal";
		}
		return i && (o.display === "block" || o.display === "flex" || o.display === "table" || o.display === "grid" || c >= r && n[qr] === "none" || a && n[qr] === "none" && c + l > r) ? "vertical" : "horizontal";
	}, Zr = function(e, t, n) {
		var r = n ? e.left : e.top, i = n ? e.right : e.bottom, a = n ? e.width : e.height, o = n ? t.left : t.top, s = n ? t.right : t.bottom, c = n ? t.width : t.height;
		return r === o || i === s || r + a / 2 === o + c / 2;
	}, Qr = function(e, t) {
		var n;
		return Ar.some(function(r) {
			var i = r[B].options.emptyInsertThreshold;
			if (!(!i || kn(r))) {
				var a = I(r), o = e >= a.left - i && e <= a.right + i, s = t >= a.top - i && t <= a.bottom + i;
				if (o && s) return n = r;
			}
		}), n;
	}, $r = function(e) {
		function t(e, n) {
			return function(r, i, a, o) {
				var s = r.options.group.name && i.options.group.name && r.options.group.name === i.options.group.name;
				if (e == null && (n || s)) return !0;
				if (e == null || e === !1) return !1;
				if (n && e === "clone") return e;
				if (typeof e == "function") return t(e(r, i, a, o), n)(r, i, a, o);
				var c = (n ? r : i).options.group.name;
				return e === !0 || typeof e == "string" && e === c || e.join && e.indexOf(c) > -1;
			};
		}
		var n = {}, r = e.group;
		(!r || gn(r) != "object") && (r = { name: r }), n.name = r.name, n.checkPull = t(r.pull, !0), n.checkPut = t(r.put), n.revertClone = r.revertClone, e.group = n;
	}, ei = function() {
		!Yr && W && F(W, "display", "none");
	}, ti = function() {
		!Yr && W && F(W, "display", "");
	}, Gr && !pr && document.addEventListener("click", function(e) {
		if (kr) return e.preventDefault(), e.stopPropagation && e.stopPropagation(), e.stopImmediatePropagation && e.stopImmediatePropagation(), kr = !1, !1;
	}, !0), ni = function(e) {
		if (H) {
			e = e.touches ? e.touches[0] : e;
			var t = Qr(e.clientX, e.clientY);
			if (t) {
				var n = {};
				for (var r in e) e.hasOwnProperty(r) && (n[r] = e[r]);
				n.target = n.rootEl = t, n.preventDefault = void 0, n.stopPropagation = void 0, t[B]._onDragOver(n);
			}
		}
	}, ri = function(e) {
		H && H.parentNode[B]._isOutsideThisEl(e.target);
	}, z.prototype = {
		constructor: z,
		_isOutsideThisEl: function(e) {
			!this.el.contains(e) && e !== this.el && (Lr = null);
		},
		_getDirection: function(e, t) {
			return typeof this.options.direction == "function" ? this.options.direction.call(this, e, t, H) : this.options.direction;
		},
		_onTapStart: function(e) {
			if (e.cancelable) {
				var t = this, n = this.el, r = this.options, i = r.preventOnFilter, a = e.type, o = e.touches && e.touches[0] || e.pointerType && e.pointerType === "touch" && e, s = (o || e).target, c = e.target.shadowRoot && (e.path && e.path[0] || e.composedPath && e.composedPath()[0]) || s, l = r.filter;
				if ($n(n), !H && !(/mousedown|pointerdown/.test(a) && e.button !== 0 || r.disabled) && !c.isContentEditable && !(!this.nativeDraggable && dr && s && s.tagName.toUpperCase() === "SELECT") && (s = N(s, r.draggable, n, !1), !(s && s.animated) && Sr !== s)) {
					if (wr = L(s), Tr = L(s, r.draggable), typeof l == "function") {
						if (l.call(this, e, s, this)) {
							R({
								sortable: t,
								rootEl: c,
								name: "filter",
								targetEl: s,
								toEl: n,
								fromEl: n
							}), V("filter", t, { evt: e }), i && e.preventDefault();
							return;
						}
					} else if (l && (l = l.split(",").some(function(r) {
						if (r = N(c, r.trim(), n, !1), r) return R({
							sortable: t,
							rootEl: r,
							name: "filter",
							targetEl: s,
							fromEl: n,
							toEl: n
						}), V("filter", t, { evt: e }), !0;
					}), l)) {
						i && e.preventDefault();
						return;
					}
					r.handle && !N(c, r.handle, n, !1) || this._prepareDragStart(e, o, s);
				}
			}
		},
		_prepareDragStart: function(e, t, n) {
			var r = this, i = r.el, a = r.options, o = i.ownerDocument, s;
			if (n && !H && n.parentNode === i) {
				var c = I(n);
				if (G = i, H = n, U = H.parentNode, xr = H.nextSibling, Sr = n, Dr = a.group, z.dragged = H, jr = {
					target: H,
					clientX: (t || e).clientX,
					clientY: (t || e).clientY
				}, Pr = jr.clientX - c.left, Fr = jr.clientY - c.top, this._lastX = (t || e).clientX, this._lastY = (t || e).clientY, H.style["will-change"] = "all", s = function() {
					if (V("delayEnded", r, { evt: e }), z.eventCanceled) {
						r._onDrop();
						return;
					}
					r._disableDelayedDragEvents(), !ur && r.nativeDraggable && (H.draggable = !0), r._triggerDragStart(e, t), R({
						sortable: r,
						name: "choose",
						originalEvent: e
					}), P(H, a.chosenClass, !0);
				}, a.ignore.split(",").forEach(function(e) {
					Tn(H, e.trim(), Kn);
				}), j(o, "dragover", ni), j(o, "mousemove", ni), j(o, "touchmove", ni), a.supportPointer ? (j(o, "pointerup", r._onDrop), !this.nativeDraggable && j(o, "pointercancel", r._onDrop)) : (j(o, "mouseup", r._onDrop), j(o, "touchend", r._onDrop), j(o, "touchcancel", r._onDrop)), ur && this.nativeDraggable && (this.options.touchStartThreshold = 4, H.draggable = !0), V("delayStart", this, { evt: e }), a.delay && (!a.delayOnTouchOnly || t) && (!this.nativeDraggable || !(lr || cr))) {
					if (z.eventCanceled) {
						this._onDrop();
						return;
					}
					a.supportPointer ? (j(o, "pointerup", r._disableDelayedDrag), j(o, "pointercancel", r._disableDelayedDrag)) : (j(o, "mouseup", r._disableDelayedDrag), j(o, "touchend", r._disableDelayedDrag), j(o, "touchcancel", r._disableDelayedDrag)), j(o, "mousemove", r._delayedDragTouchMoveHandler), j(o, "touchmove", r._delayedDragTouchMoveHandler), a.supportPointer && j(o, "pointermove", r._delayedDragTouchMoveHandler), r._dragStartTimer = setTimeout(s, a.delay);
				} else s();
			}
		},
		_delayedDragTouchMoveHandler: function(e) {
			var t = e.touches ? e.touches[0] : e;
			Math.max(Math.abs(t.clientX - this._lastX), Math.abs(t.clientY - this._lastY)) >= Math.floor(this.options.touchStartThreshold / (this.nativeDraggable && window.devicePixelRatio || 1)) && this._disableDelayedDrag();
		},
		_disableDelayedDrag: function() {
			H && Kn(H), clearTimeout(this._dragStartTimer), this._disableDelayedDragEvents();
		},
		_disableDelayedDragEvents: function() {
			var e = this.el.ownerDocument;
			M(e, "mouseup", this._disableDelayedDrag), M(e, "touchend", this._disableDelayedDrag), M(e, "touchcancel", this._disableDelayedDrag), M(e, "pointerup", this._disableDelayedDrag), M(e, "pointercancel", this._disableDelayedDrag), M(e, "mousemove", this._delayedDragTouchMoveHandler), M(e, "touchmove", this._delayedDragTouchMoveHandler), M(e, "pointermove", this._delayedDragTouchMoveHandler);
		},
		_triggerDragStart: function(e, t) {
			t ||= e.pointerType == "touch" && e, !this.nativeDraggable || t ? this.options.supportPointer ? j(document, "pointermove", this._onTouchMove) : t ? j(document, "touchmove", this._onTouchMove) : j(document, "mousemove", this._onTouchMove) : (j(H, "dragend", this), j(G, "dragstart", this._onDragStart));
			try {
				document.selection ? er(function() {
					document.selection.empty();
				}) : window.getSelection().removeAllRanges();
			} catch {}
		},
		_dragStarted: function(e, t) {
			if (Or = !1, G && H) {
				V("dragStarted", this, { evt: t }), this.nativeDraggable && j(document, "dragover", ri);
				var n = this.options;
				!e && P(H, n.dragClass, !1), P(H, n.ghostClass, !0), z.active = this, e && this._appendGhost(), R({
					sortable: this,
					name: "start",
					originalEvent: t
				});
			} else this._nulling();
		},
		_emulateDragOver: function() {
			if (Y) {
				this._lastX = Y.clientX, this._lastY = Y.clientY, ei();
				for (var e = document.elementFromPoint(Y.clientX, Y.clientY), t = e; e && e.shadowRoot && (e = e.shadowRoot.elementFromPoint(Y.clientX, Y.clientY), e !== t);) t = e;
				if (H.parentNode[B]._isOutsideThisEl(e), t) do {
					if (t[B]) {
						var n = void 0;
						if (n = t[B]._onDragOver({
							clientX: Y.clientX,
							clientY: Y.clientY,
							target: e,
							rootEl: t
						}), n && !this.options.dragoverBubble) break;
					}
					e = t;
				} while (t = Cn(t));
				ti();
			}
		},
		_onTouchMove: function(e) {
			if (jr) {
				var t = this.options, n = t.fallbackTolerance, r = t.fallbackOffset, i = e.touches ? e.touches[0] : e, a = W && wn(W, !0), o = W && a && a.a, s = W && a && a.d, c = Kr && X && An(X), l = (i.clientX - jr.clientX + r.x) / (o || 1) + (c ? c[0] - Hr[0] : 0) / (o || 1), u = (i.clientY - jr.clientY + r.y) / (s || 1) + (c ? c[1] - Hr[1] : 0) / (s || 1);
				if (!z.active && !Or) {
					if (n && Math.max(Math.abs(i.clientX - this._lastX), Math.abs(i.clientY - this._lastY)) < n) return;
					this._onDragStart(e, !0);
				}
				if (W) {
					a ? (a.e += l - (Mr || 0), a.f += u - (Nr || 0)) : a = {
						a: 1,
						b: 0,
						c: 0,
						d: 1,
						e: l,
						f: u
					};
					var d = `matrix(${a.a},${a.b},${a.c},${a.d},${a.e},${a.f})`;
					F(W, "webkitTransform", d), F(W, "mozTransform", d), F(W, "msTransform", d), F(W, "transform", d), Mr = l, Nr = u, Y = i;
				}
				e.cancelable && e.preventDefault();
			}
		},
		_appendGhost: function() {
			if (!W) {
				var e = this.options.fallbackOnBody ? document.body : G, t = I(H, !0, Kr, !0, e), n = this.options;
				if (Kr) {
					for (X = e; F(X, "position") === "static" && F(X, "transform") === "none" && X !== document;) X = X.parentNode;
					X !== document.body && X !== document.documentElement ? (X === document && (X = En()), t.top += X.scrollTop, t.left += X.scrollLeft) : X = En(), Hr = An(X);
				}
				W = H.cloneNode(!0), P(W, n.ghostClass, !1), P(W, n.fallbackClass, !0), P(W, n.dragClass, !0), F(W, "transition", ""), F(W, "transform", ""), F(W, "box-sizing", "border-box"), F(W, "margin", 0), F(W, "top", t.top), F(W, "left", t.left), F(W, "width", t.width), F(W, "height", t.height), F(W, "opacity", "0.8"), F(W, "position", Kr ? "absolute" : "fixed"), F(W, "zIndex", "100000"), F(W, "pointerEvents", "none"), z.ghost = W, e.appendChild(W), F(W, "transform-origin", Pr / parseInt(W.style.width) * 100 + "% " + Fr / parseInt(W.style.height) * 100 + "%");
			}
		},
		_onDragStart: function(e, t) {
			var n = this, r = e.dataTransfer, i = n.options;
			if (V("dragStart", this, { evt: e }), z.eventCanceled) {
				this._onDrop();
				return;
			}
			V("setupClone", this), z.eventCanceled || (K = Rn(H), K.removeAttribute("id"), K.draggable = !1, K.style["will-change"] = "", this._hideClone(), P(K, this.options.chosenClass, !1), z.clone = K), n.cloneId = er(function() {
				V("clone", n), !z.eventCanceled && (n.options.removeCloneOnHide || G.insertBefore(K, H), n._hideClone(), R({
					sortable: n,
					name: "clone"
				}));
			}), !t && P(H, i.dragClass, !0), t ? (kr = !0, n._loopId = setInterval(n._emulateDragOver, 50)) : (M(document, "mouseup", n._onDrop), M(document, "touchend", n._onDrop), M(document, "touchcancel", n._onDrop), r && (r.effectAllowed = "move", i.setData && i.setData.call(n, r, H)), j(document, "drop", n), F(H, "transform", "translateZ(0)")), Or = !0, n._dragStartId = er(n._dragStarted.bind(n, t, e)), j(document, "selectstart", n), Ir = !0, window.getSelection().removeAllRanges(), dr && F(document.body, "user-select", "none");
		},
		_onDragOver: function(e) {
			var t = this.el, n = e.target, r, i, a, o = this.options, s = o.group, c = z.active, l = Dr === s, u = o.sort, d = J || c, f, p = this, m = !1;
			if (Ur) return;
			function h(o, s) {
				V(o, p, hn({
					evt: e,
					isOwner: l,
					axis: f ? "vertical" : "horizontal",
					revert: a,
					dragRect: r,
					targetRect: i,
					canSort: u,
					fromSortable: d,
					target: n,
					completed: _,
					onMove: function(n, i) {
						return Gn(G, t, H, r, n, I(n), e, i);
					},
					changed: v
				}, s));
			}
			function g() {
				h("dragOverAnimationCapture"), p.captureAnimationState(), p !== d && d.captureAnimationState();
			}
			function _(r) {
				return h("dragOverCompleted", { insertion: r }), r && (l ? c._hideClone() : c._showClone(p), p !== d && (P(H, J ? J.options.ghostClass : c.options.ghostClass, !1), P(H, o.ghostClass, !0)), J !== p && p !== z.active ? J = p : p === z.active && J && (J = null), d === p && (p._ignoreWhileAnimating = n), p.animateAll(function() {
					h("dragOverAnimationComplete"), p._ignoreWhileAnimating = null;
				}), p !== d && (d.animateAll(), d._ignoreWhileAnimating = null)), (n === H && !H.animated || n === t && !n.animated) && (Lr = null), !o.dragoverBubble && !e.rootEl && n !== document && (H.parentNode[B]._isOutsideThisEl(e.target), !r && ni(e)), !o.dragoverBubble && e.stopPropagation && e.stopPropagation(), m = !0;
			}
			function v() {
				q = L(H), Er = L(H, o.draggable), R({
					sortable: p,
					name: "change",
					toEl: t,
					newIndex: q,
					newDraggableIndex: Er,
					originalEvent: e
				});
			}
			if (e.preventDefault !== void 0 && e.cancelable && e.preventDefault(), n = N(n, o.draggable, t, !0), h("dragOver"), z.eventCanceled) return m;
			if (H.contains(e.target) || n.animated && n.animatingX && n.animatingY || p._ignoreWhileAnimating === n) return _(!1);
			if (kr = !1, c && !o.disabled && (l ? u || (a = U !== G) : J === this || (this.lastPutMode = Dr.checkPull(this, c, H, e)) && s.checkPut(this, c, H, e))) {
				if (f = this._getDirection(e, n) === "vertical", r = I(H), h("dragOverValid"), z.eventCanceled) return m;
				if (a) return U = G, g(), this._hideClone(), h("revert"), z.eventCanceled || (xr ? G.insertBefore(H, xr) : G.appendChild(H)), _(!0);
				var y = kn(t, o.draggable);
				if (!y || Yn(e, f, this) && !y.animated) {
					if (y === H) return _(!1);
					if (y && t === e.target && (n = y), n && (i = I(n)), Gn(G, t, H, r, n, i, e, !!n) !== !1) return g(), y && y.nextSibling ? t.insertBefore(H, y.nextSibling) : t.appendChild(H), U = t, v(), _(!0);
				} else if (y && Jn(e, f, this)) {
					var b = On(t, 0, o, !0);
					if (b === H) return _(!1);
					if (n = b, i = I(n), Gn(G, t, H, r, n, i, e, !1) !== !1) return g(), t.insertBefore(H, b), U = t, v(), _(!0);
				} else if (n.parentNode === t) {
					i = I(n);
					var x = 0, ee, te = H.parentNode !== t, ne = !Zr(H.animated && H.toRect || r, n.animated && n.toRect || i, f), S = f ? "top" : "left", re = Dn(n, "top", "top") || Dn(H, "top", "top"), ie = re ? re.scrollTop : void 0;
					Lr !== n && (ee = i[S], zr = !1, Br = !ne && o.invertSwap || te), x = Xn(e, n, i, f, ne ? 1 : o.swapThreshold, o.invertedSwapThreshold == null ? o.swapThreshold : o.invertedSwapThreshold, Br, Lr === n);
					var C;
					if (x !== 0) {
						var ae = L(H);
						do
							ae -= x, C = U.children[ae];
						while (C && (F(C, "display") === "none" || C === W));
					}
					if (x === 0 || C === n) return _(!1);
					Lr = n, Rr = x;
					var oe = n.nextElementSibling, se = !1;
					se = x === 1;
					var ce = Gn(G, t, H, r, n, i, e, se);
					if (ce !== !1) return (ce === 1 || ce === -1) && (se = ce === 1), Ur = !0, setTimeout(qn, 30), g(), se && !oe ? t.appendChild(H) : n.parentNode.insertBefore(H, se ? oe : n), re && Ln(re, 0, ie - re.scrollTop), U = H.parentNode, ee !== void 0 && !Br && (Vr = Math.abs(ee - I(n)[S])), v(), _(!0);
				}
				if (t.contains(H)) return _(!1);
			}
			return !1;
		},
		_ignoreWhileAnimating: null,
		_offMoveEvents: function() {
			M(document, "mousemove", this._onTouchMove), M(document, "touchmove", this._onTouchMove), M(document, "pointermove", this._onTouchMove), M(document, "dragover", ni), M(document, "mousemove", ni), M(document, "touchmove", ni);
		},
		_offUpEvents: function() {
			var e = this.el.ownerDocument;
			M(e, "mouseup", this._onDrop), M(e, "touchend", this._onDrop), M(e, "pointerup", this._onDrop), M(e, "pointercancel", this._onDrop), M(e, "touchcancel", this._onDrop), M(document, "selectstart", this);
		},
		_onDrop: function(e) {
			var t = this.el, n = this.options;
			if (q = L(H), Er = L(H, n.draggable), V("drop", this, { evt: e }), U = H && H.parentNode, q = L(H), Er = L(H, n.draggable), z.eventCanceled) {
				this._nulling();
				return;
			}
			Or = !1, Br = !1, zr = !1, clearInterval(this._loopId), clearTimeout(this._dragStartTimer), tr(this.cloneId), tr(this._dragStartId), this.nativeDraggable && (M(document, "drop", this), M(t, "dragstart", this._onDragStart)), this._offMoveEvents(), this._offUpEvents(), dr && F(document.body, "user-select", ""), F(H, "transform", ""), e && (Ir && (e.cancelable && e.preventDefault(), !n.dropBubble && e.stopPropagation()), W && W.parentNode && W.parentNode.removeChild(W), (G === U || J && J.lastPutMode !== "clone") && K && K.parentNode && K.parentNode.removeChild(K), H && (this.nativeDraggable && M(H, "dragend", this), Kn(H), H.style["will-change"] = "", Ir && !Or && P(H, J ? J.options.ghostClass : this.options.ghostClass, !1), P(H, this.options.chosenClass, !1), R({
				sortable: this,
				name: "unchoose",
				toEl: U,
				newIndex: null,
				newDraggableIndex: null,
				originalEvent: e
			}), G === U ? q !== wr && q >= 0 && (R({
				sortable: this,
				name: "update",
				toEl: U,
				originalEvent: e
			}), R({
				sortable: this,
				name: "sort",
				toEl: U,
				originalEvent: e
			})) : (q >= 0 && (R({
				rootEl: U,
				name: "add",
				toEl: U,
				fromEl: G,
				originalEvent: e
			}), R({
				sortable: this,
				name: "remove",
				toEl: U,
				originalEvent: e
			}), R({
				rootEl: U,
				name: "sort",
				toEl: U,
				fromEl: G,
				originalEvent: e
			}), R({
				sortable: this,
				name: "sort",
				toEl: U,
				originalEvent: e
			})), J && J.save()), z.active && ((q == null || q === -1) && (q = wr, Er = Tr), R({
				sortable: this,
				name: "end",
				toEl: U,
				originalEvent: e
			}), this.save()))), this._nulling();
		},
		_nulling: function() {
			V("nulling", this), G = H = U = W = xr = K = Sr = Cr = jr = Y = Ir = q = Er = wr = Tr = Lr = Rr = J = Dr = z.dragged = z.ghost = z.clone = z.active = null, Wr.forEach(function(e) {
				e.checked = !0;
			}), Wr.length = Mr = Nr = 0;
		},
		handleEvent: function(e) {
			switch (e.type) {
				case "drop":
				case "dragend":
					this._onDrop(e);
					break;
				case "dragenter":
				case "dragover":
					H && (this._onDragOver(e), Wn(e));
					break;
				case "selectstart":
					e.preventDefault();
					break;
			}
		},
		toArray: function() {
			for (var e = [], t, n = this.el.children, r = 0, i = n.length, a = this.options; r < i; r++) t = n[r], N(t, a.draggable, this.el, !1) && e.push(t.getAttribute(a.dataIdAttr) || Qn(t));
			return e;
		},
		sort: function(e, t) {
			var n = {}, r = this.el;
			this.toArray().forEach(function(e, t) {
				var i = r.children[t];
				N(i, this.options.draggable, r, !1) && (n[e] = i);
			}, this), t && this.captureAnimationState(), e.forEach(function(e) {
				n[e] && (r.removeChild(n[e]), r.appendChild(n[e]));
			}), t && this.animateAll();
		},
		save: function() {
			var e = this.options.store;
			e && e.set && e.set(this);
		},
		closest: function(e, t) {
			return N(e, t || this.options.draggable, this.el, !1);
		},
		option: function(e, t) {
			var n = this.options;
			if (t === void 0) return n[e];
			var r = yr.modifyOption(this, e, t);
			r === void 0 ? n[e] = t : n[e] = r, e === "group" && $r(n);
		},
		destroy: function() {
			V("destroy", this);
			var e = this.el;
			e[B] = null, M(e, "mousedown", this._onTapStart), M(e, "touchstart", this._onTapStart), M(e, "pointerdown", this._onTapStart), this.nativeDraggable && (M(e, "dragover", this), M(e, "dragenter", this)), Array.prototype.forEach.call(e.querySelectorAll("[draggable]"), function(e) {
				e.removeAttribute("draggable");
			}), this._onDrop(), this._disableDelayedDragEvents(), Ar.splice(Ar.indexOf(this.el), 1), this.el = e = null;
		},
		_hideClone: function() {
			if (!Cr) {
				if (V("hideClone", this), z.eventCanceled) return;
				F(K, "display", "none"), this.options.removeCloneOnHide && K.parentNode && K.parentNode.removeChild(K), Cr = !0;
			}
		},
		_showClone: function(e) {
			if (e.lastPutMode !== "clone") {
				this._hideClone();
				return;
			}
			if (Cr) {
				if (V("showClone", this), z.eventCanceled) return;
				H.parentNode == G && !this.options.group.revertClone ? G.insertBefore(K, H) : xr ? G.insertBefore(K, xr) : G.appendChild(K), this.options.group.revertClone && this.animate(H, K), F(K, "display", ""), Cr = !1;
			}
		}
	}, Gr && j(document, "touchmove", function(e) {
		(z.active || Or) && e.cancelable && e.preventDefault();
	}), z.utils = {
		on: j,
		off: M,
		css: F,
		find: Tn,
		is: function(e, t) {
			return !!N(e, t, e, !1);
		},
		extend: Nn,
		throttle: Fn,
		closest: N,
		toggleClass: P,
		clone: Rn,
		index: L,
		nextTick: er,
		cancelNextTick: tr,
		detectDirection: Xr,
		getChild: On,
		expando: B
	}, z.get = function(e) {
		return e[B];
	}, z.mount = function() {
		var e = [...arguments];
		e[0].constructor === Array && (e = e[0]), e.forEach(function(e) {
			if (!e.prototype || !e.prototype.constructor) throw `Sortable: Mounted plugin must be a constructor function, not ${{}.toString.call(e)}`;
			e.utils && (z.utils = hn(hn({}, z.utils), e.utils)), yr.mount(e);
		});
	}, z.create = function(e, t) {
		return new z(e, t);
	}, z.version = sr, Z = [], oi = !1, di = Fn(function(e, t, n, r) {
		if (t.scroll) {
			var i = (e.touches ? e.touches[0] : e).clientX, a = (e.touches ? e.touches[0] : e).clientY, o = t.scrollSensitivity, s = t.scrollSpeed, c = En(), l = !1, u;
			ai !== n && (ai = n, rr(), ii = t.scroll, u = t.scrollFn, ii === !0 && (ii = Mn(n, !0)));
			var d = 0, f = ii;
			do {
				var p = f, m = I(p), h = m.top, g = m.bottom, _ = m.left, v = m.right, y = m.width, b = m.height, x = void 0, ee = void 0, te = p.scrollWidth, ne = p.scrollHeight, S = F(p), re = p.scrollLeft, ie = p.scrollTop;
				p === c ? (x = y < te && (S.overflowX === "auto" || S.overflowX === "scroll" || S.overflowX === "visible"), ee = b < ne && (S.overflowY === "auto" || S.overflowY === "scroll" || S.overflowY === "visible")) : (x = y < te && (S.overflowX === "auto" || S.overflowX === "scroll"), ee = b < ne && (S.overflowY === "auto" || S.overflowY === "scroll"));
				var C = x && (Math.abs(v - i) <= o && re + y < te) - (Math.abs(_ - i) <= o && !!re), ae = ee && (Math.abs(g - a) <= o && ie + b < ne) - (Math.abs(h - a) <= o && !!ie);
				if (!Z[d]) for (var oe = 0; oe <= d; oe++) Z[oe] || (Z[oe] = {});
				(Z[d].vx != C || Z[d].vy != ae || Z[d].el !== p) && (Z[d].el = p, Z[d].vx = C, Z[d].vy = ae, clearInterval(Z[d].pid), (C != 0 || ae != 0) && (l = !0, Z[d].pid = setInterval(function() {
					r && this.layer === 0 && z.active._onTouchMove(li);
					var t = Z[this.layer].vy ? Z[this.layer].vy * s : 0, n = Z[this.layer].vx ? Z[this.layer].vx * s : 0;
					typeof u == "function" && u.call(z.dragged.parentNode[B], n, t, e, li, Z[this.layer].el) !== "continue" || Ln(Z[this.layer].el, n, t);
				}.bind({ layer: d }), 24))), d++;
			} while (t.bubbleScroll && f !== c && (f = Mn(f, !1)));
			oi = l;
		}
	}, 30), fi = function(e) {
		var t = e.originalEvent, n = e.putSortable, r = e.dragEl, i = e.activeSortable, a = e.dispatchSortableEvent, o = e.hideGhostForTarget, s = e.unhideGhostForTarget;
		if (t) {
			var c = n || i;
			o();
			var l = t.changedTouches && t.changedTouches.length ? t.changedTouches[0] : t, u = document.elementFromPoint(l.clientX, l.clientY);
			s(), c && !c.el.contains(u) && (a("spill"), this.onSpill({
				dragEl: r,
				putSortable: n
			}));
		}
	}, ar.prototype = {
		startIndex: null,
		dragStart: function(e) {
			this.startIndex = e.oldDraggableIndex;
		},
		onSpill: function(e) {
			var t = e.dragEl, n = e.putSortable;
			this.sortable.captureAnimationState(), n && n.captureAnimationState();
			var r = On(this.sortable.el, this.startIndex, this.options);
			r ? this.sortable.el.insertBefore(t, r) : this.sortable.el.appendChild(t), this.sortable.animateAll(), n && n.animateAll();
		},
		drop: fi
	}, vn(ar, { pluginName: "revertOnSpill" }), or.prototype = {
		onSpill: function(e) {
			var t = e.dragEl, n = e.putSortable || this.sortable;
			n.captureAnimationState(), t.parentNode && t.parentNode.removeChild(t), n.animateAll();
		},
		drop: fi
	}, vn(or, { pluginName: "removeOnSpill" }), z.mount(new nr()), z.mount(or, ar);
})), mi, hi, gi, _i, vi, yi, bi, xi, Si, Ci = t((() => {
	d(), He(), we(), hi = .4, gi = class {
		constructor() {
			this.handlers = [], this.toolbarConnected = (e) => {
				this.toolbar = e, this.reactionDisposer = x(() => Ve.getToolbarExpandMode(), (e, t) => {
					if (!this.toolbar) return;
					if (t) {
						let e = this.handlers.find((e) => e.getMode() === t);
						e && e.onDeactivate(this.toolbar);
					}
					let n = this.handlers.find((t) => t.getMode() === e);
					n && (this.activeExpandModeHandler = n, this.activeExpandModeHandler.onActivate(this.toolbar));
				}, { fireImmediately: !0 });
			}, this.toolbarDisconnected = () => {
				this.activeExpandModeHandler && this.toolbar && this.activeExpandModeHandler.onDeactivate(this.toolbar), this.reactionDisposer && this.reactionDisposer(), this.toolbar = void 0;
			}, this.devToolsPopoverOpenClosedChanged = (e) => {
				this.activeExpandModeHandler && this.activeExpandModeHandler.devToolsPopoverOpenClosedChanged(e);
			}, this.handlers.push(new vi()), this.handlers.push(new yi()), this.handlers.push(new bi()), this.handlers.push(new xi()), this.handlers.push(new Si());
		}
	}, _i = class {
		constructor() {
			this.isDevToolsPopoverOpen = !1;
		}
		devToolsPopoverOpenClosedChanged(e) {
			this.isDevToolsPopoverOpen = e;
		}
		getIdleWidth(e) {
			let t = e.radioGroup, n = Number.parseFloat(getComputedStyle(document.documentElement).fontSize), r = Number.parseFloat(getComputedStyle(t).getPropertyValue("--copilot-size-md")) * n;
			return n * .125 * 2 + r;
		}
		shrink() {
			this.toolbar && (this.isDevToolsPopoverOpen || (this.updateToolbarOpacity(!0), this.toolbar.setPlayModeIconProgress(0), this.toolbar.radioGroup.style.width = `${this.getIdleWidth(this.toolbar)}px`, this.toolbar.updateToolbarTotalWidth(), this.toolbar.removeAttribute("modes-expanded")));
		}
		updateToolbarOpacity(e) {
			this.toolbar && (e ? this.toolbar.style.opacity = `${hi}` : this.toolbar.style.opacity = "1");
		}
	}, vi = class e extends _i {
		constructor(...t) {
			super(...t), this.lastDocumentMouseClientX = 0, this.lastDocumentMouseClientY = 0, this.ticking = !1, this.handlePointerProximity = (e) => {
				this.toolbar && (this.lastDocumentMouseClientX = e.clientX, this.lastDocumentMouseClientY = e.clientY, this.ticking ||= (requestAnimationFrame(() => {
					let t = this.toolbar;
					if (t) {
						if (t.isDragging || t.matches(":hover") || t.matches(":focus-within") || this.isDevToolsPopoverOpen) {
							t.expandRadioButtons(), this.ticking = !1;
							return;
						}
						if (O.activeMode !== "play") {
							t.expandRadioButtons(), this.ticking = !1;
							return;
						}
						this.updateRadioButtonExpandAndStylingByPointerLocation(e.clientX, e.clientY), this.ticking = !1;
					}
				}), !0));
			}, this.updateRadioButtonExpandAndStylingByPointerLocation = (t, n) => {
				if (!this.toolbar) return;
				if (O.activeMode !== "play" && !this.toolbar.hasAttribute("modes-expanded")) {
					this.toolbar.expandRadioButtons();
					return;
				}
				let r = this.toolbar.radioGroup, i = r.style.width, a = this.toolbar.getBoundingClientRect(), o = Math.max(a.left - t, 0, t - a.right), s = Math.max(a.top - n, 0, n - a.bottom), c = Math.max(0, 1 - Math.hypot(o, s) / e.PROXIMITY_RADIUS), l = Number.parseFloat(getComputedStyle(r).getPropertyValue("--copilot-toolbar-mode-width")), u = this.getIdleWidth(this.toolbar), d = hi + (1 - hi) * c;
				this.toolbar.style.opacity = d.toFixed(2), this.toolbar.setPlayModeIconProgress(c);
				let f = u + (l - u) * c;
				r.style.width = `${f.toFixed(2)}px`, i !== r.style.width && this.toolbar.updateToolbarTotalWidth(), this.toolbar.toggleAttribute("modes-expanded", f === l);
			};
		}
		onActivate(e) {
			this.toolbar = e, this.activeModeReactionDisposer = x(() => O.activeMode, () => {
				O.activeMode !== "play" && this.toolbar ? (this.updateToolbarOpacity(!1), this.toolbar.expandRadioButtons()) : this.updateRadioButtonExpandAndStylingByPointerLocation(this.lastDocumentMouseClientX, this.lastDocumentMouseClientY);
			}, { fireImmediately: !0 }), document.addEventListener("mousemove", this.handlePointerProximity);
		}
		getMode() {
			return "proximity";
		}
		onDeactivate(e) {
			document.removeEventListener("mousemove", this.handlePointerProximity), this.activeModeReactionDisposer && this.activeModeReactionDisposer();
		}
		devToolsPopoverOpenClosedChanged(e) {
			super.devToolsPopoverOpenClosedChanged(e), this.isDevToolsPopoverOpen || this.updateRadioButtonExpandAndStylingByPointerLocation(this.lastDocumentMouseClientX, this.lastDocumentMouseClientY);
		}
	}, mi = vi, mi.PROXIMITY_RADIUS = 180, yi = class extends _i {
		constructor(...e) {
			super(...e), this.mouseLeft = !1, this.handleMouseOverEvent = (e) => {
				this.toolbar && (this.mouseLeft = !1, this.toolbar.expandRadioButtons(), this.toolbar.updateToolbarTotalWidth());
			}, this.handleMouseLeaveEvent = (e) => {
				this.mouseLeft = !0, O.activeMode === "play" && this.shrink();
			};
		}
		getMode() {
			return "hover";
		}
		onActivate(e) {
			this.toolbar = e, e.addEventListener("mouseover", this.handleMouseOverEvent), e.addEventListener("mouseleave", this.handleMouseLeaveEvent), this.activeModeReactionDisposer = x(() => O.activeMode, () => {
				this.updateByCopilotActiveMode();
			}, { fireImmediately: !0 });
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
	}, bi = class extends _i {
		constructor(...e) {
			super(...e), this.onPlayClickAction = "expand", this.handleClick = (e) => {
				if (O.activeMode === "play" && e.target && e.target instanceof HTMLElement) {
					let t = e.target.closest("vaadin-radio-button");
					t?.dataset.mode && (t.dataset.mode === "play" ? this.onPlayClickAction === "expand" ? (this.toolbar?.expandRadioButtons(), this.onPlayClickAction = "collapse") : (this.shrink(), this.onPlayClickAction = "expand") : (this.toolbar?.expandRadioButtons(), this.onPlayClickAction = "collapse"));
				}
			};
		}
		getMode() {
			return "click";
		}
		onActivate(e) {
			this.toolbar = e, e.addEventListener("click", this.handleClick), requestAnimationFrame(() => {
				this.updateByCopilotActiveMode();
			}), this.activeModeReactionDisposer = x(() => O.activeMode, () => {
				this.updateByCopilotActiveMode();
			}, { fireImmediately: !0 });
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
	}, xi = class extends _i {
		getMode() {
			return "always";
		}
		onActivate(e) {
			this.toolbar = e, e.expandRadioButtons();
		}
		onDeactivate(e) {}
	}, Si = class extends _i {
		constructor(...e) {
			super(...e), this.handleMouseOverListener = (e) => {
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
	};
})), wi, Ti, Ei, Di = t((() => {
	me(), de(), we(), v(), he(), tt(), g(), pi(), ye(), Ce(), Ie(), ce(), Ci(), He(), Ze(), fe(), Ei = (wi = class extends pe {
		constructor(...e) {
			super(...e), this.sortableInitialized = !1, this.modeRadioButtonItems = [], this.devToolsDataLoading = !1, this.devToolsDataLoaded = !1, this.isDragging = !1, this.dragPreparing = !1, this.dragStartX = 0, this.dragStartY = 0, this.toolbarStartRight = 0, this.toolbarStartTop = 0, this.wasDraggedInLastInteraction = !1, this.dragThreshold = 5, this.expandModeHandler = new gi(), this.transitionStart = (e) => {
				e.propertyName === "width" && this.setAttribute(Ti.TRANSITION_STATUS_ATTR_KEY, "started");
			}, this.transitionEnd = (e) => {
				e.propertyName === "width" && (this.setAttribute(Ti.TRANSITION_STATUS_ATTR_KEY, "ended"), this.constrainToViewport());
			}, this.handleMouseDown = (e) => {
				if (e.button !== 0) return;
				let t = this.querySelector("#devtools-button");
				if (!t || !(e.target instanceof Node) || !t.contains(e.target)) return;
				this.dragPreparing = !0, this.dragStartX = e.clientX, this.dragStartY = e.clientY;
				let n = this.getBoundingClientRect();
				this.toolbarStartRight = window.innerWidth - n.right, this.toolbarStartTop = n.top, document.addEventListener("mousemove", this.handleMouseMove), document.addEventListener("mouseup", this.handleMouseUp);
			}, this.handleMouseMove = (e) => {
				if (!this.dragPreparing && !this.isDragging) return;
				let t = e.clientX - this.dragStartX, n = e.clientY - this.dragStartY, r = Math.sqrt(t * t + n * n);
				if (this.dragPreparing && r > this.dragThreshold && (this.isDragging = !0, this.dragPreparing = !1, this.style.transition = "none"), this.isDragging) {
					let e = this.toolbarStartRight - t, r = this.toolbarStartTop + n, i = this.getBoundingClientRect(), a = i.width, o = i.height;
					e = Math.max(0, e), window.innerWidth - e - a < 0 && (e = window.innerWidth - a), r = Math.max(0, r);
					let s = window.innerHeight - o;
					r = Math.min(r, s), this.style.right = `${e}px`, this.style.top = `${r}px`, this.style.bottom = "auto", this.style.left = "auto";
				}
			}, this.handleMouseUp = () => {
				let e = this.isDragging;
				if (this.isDragging = !1, this.dragPreparing = !1, this.wasDraggedInLastInteraction = e, e) {
					this.style.transition = "";
					let e = this.getBoundingClientRect(), t = window.innerWidth - e.right, n = e.top;
					se.saveToolbarPosition(t, n);
				}
				document.removeEventListener("mousemove", this.handleMouseMove), document.removeEventListener("mouseup", this.handleMouseUp), setTimeout(() => {
					this.wasDraggedInLastInteraction = !1;
				}, 10);
			}, this.handleClick = (e) => {
				this.wasDraggedInLastInteraction && (e.stopPropagation(), e.preventDefault());
			}, this.handleWindowResize = () => {
				this.constrainToViewport(!0);
			}, this.expandRadioButtons = () => {
				if (this.hasAttribute("modes-expanded")) return;
				let e = this.radioGroup.style.width;
				this.style.opacity = "1", this.setPlayModeIconProgress(1);
				let t = Number.parseFloat(getComputedStyle(this.radioGroup).getPropertyValue("--copilot-toolbar-mode-width"));
				this.radioGroup.style.width = `${t.toFixed(2)}px`, e !== this.radioGroup.style.width && this.updateToolbarTotalWidth(), this.toggleAttribute("modes-expanded", !0);
			}, this.onSortEndEvent = (e, t, n) => {
				if (!(e.item instanceof HTMLElement)) return;
				let r = e.item.dataset.panelTag;
				if (!r || !E.getPanelByTag(r)) return;
				let i = this.getOrderByTag(t);
				E.reOrderPanels(i, n);
			};
		}
		connectedCallback() {
			super.connectedCallback(), this.popover = "manual", this.setPlayModeIconProgress(1), this.modeRadioButtonItems = Object.entries(Se).map(([e, t]) => ({
				value: e,
				label: t.label,
				order: t.toolbarOrder,
				icon: t.toolbarIcon
			})), this.modeRadioButtonItems.sort((e, t) => e.order - t.order), this.classList.add("backdrop-blur-sm", "bg-gray-1", "dark:bg-gray-5", "border", "bottom-4", "duration-300", "end-4", "fixed", "flex", "justify-end", "m-0", "overflow-hidden", "p-1", "pointer-events-auto", "rounded-toolbar", "shadow-xl", "start-auto", "top-auto", "transition-all", "z-100");
			let e = se.getToolbarPosition();
			if (e) {
				let t = e.right, n = e.top;
				this.style.right = `${t}px`, this.style.top = `${n}px`, this.style.bottom = "auto", this.style.left = "auto";
			}
			this.addEventListener("transitionstart", this.transitionStart), this.addEventListener("transitionend", this.transitionEnd), this.addEventListener("mousedown", this.handleMouseDown), this.addEventListener("click", this.handleClick, !0), window.addEventListener("resize", this.handleWindowResize), this.reaction(() => O.userInfo, () => {
				O.userInfo && this.devToolsPopover.requestContentUpdate();
			}), this.reaction(() => Ve.getSelectedSize(), () => {
				requestAnimationFrame(() => {
					let e = this.radioGroup;
					if (this.hasAttribute("modes-expanded")) {
						let t = Number.parseFloat(getComputedStyle(e).getPropertyValue("--copilot-toolbar-mode-width"));
						e.style.width = `${t.toFixed(2)}px`;
					} else {
						let t = Number.parseFloat(getComputedStyle(document.documentElement).fontSize), n = Number.parseFloat(getComputedStyle(e).getPropertyValue("--copilot-size-md")) * t;
						e.style.width = `${(t * .125 * 2 + n).toFixed(2)}px`;
					}
					this.updateToolbarTotalWidth();
				});
			}, { fireImmediately: !0 }), this.reaction(() => O.activeMode, () => {
				this.queryModeRadioButtons().forEach((e) => {
					let t = e;
					t._setFocused && t._setFocused(!1);
				});
				let e = this.queryRadioButtonForMode(O.activeMode);
				e?._setFocused && e._setFocused(!0);
			});
		}
		disconnectedCallback() {
			super.disconnectedCallback(), this.removeEventListener("transitionstart", this.transitionStart), this.removeEventListener("transitionend", this.transitionEnd), this.removeEventListener("mousedown", this.handleMouseDown), this.removeEventListener("click", this.handleClick, !0), document.removeEventListener("mousemove", this.handleMouseMove), document.removeEventListener("mouseup", this.handleMouseUp), window.removeEventListener("resize", this.handleWindowResize), this.expandModeHandler.toolbarDisconnected();
		}
		constrainToViewport(e = !1) {
			if (!this.style.right || this.style.right === "") return;
			let t = this.getBoundingClientRect(), n = t.width, r = t.height;
			if (n === 0 || r === 0) return;
			let i = parseFloat(this.style.right), a = parseFloat(this.style.top || "0"), o = i, s = a;
			i = Math.max(0, i), window.innerWidth - i - n < 0 && (i = window.innerWidth - n), a = Math.max(0, a);
			let c = window.innerHeight - r;
			a = Math.min(a, c), this.style.right = `${i}px`, this.style.top = `${a}px`, e && (i !== o || a !== s) && se.saveToolbarPosition(i, a);
		}
		createRenderRoot() {
			return this;
		}
		firstUpdated(e) {
			super.firstUpdated(e), this.expandModeHandler.toolbarConnected(this);
		}
		updated(e) {
			super.updated(e), this.updateToolbarTotalWidth(), this.modePanelIconContainer !== null && !this.sortableInitialized && (new z(this.modePanelIconContainer, {
				animation: 150,
				draggable: ".panel-icon-button",
				onStart: (e) => {
					O.setToolbarPanelIconDragging(!0);
				},
				onEnd: (e) => {
					O.setToolbarPanelIconDragging(!1), this.onSortEndEvent(e, this.modePanelIconContainer, O.activeMode);
				}
			}), this.sortableInitialized = !0);
		}
		updateToolbarTotalWidth() {
			this.classList.add("transition-none");
			let e = [...this.querySelectorAll("#mode-panel-icon-container, #mode-icon-container, #common-mode-panel-icon-container")].reduce((e, t) => e + t.offsetWidth, 0);
			this.style.width = `${e}px`, this.classList.remove("transition-none");
		}
		setPlayModeIconProgress(e) {
			let t = Math.min(1, Math.max(0, e));
			this.style.setProperty(Ti.PLAY_MODE_ICON_PROGRESS_CSS_VAR, t.toFixed(2));
		}
		render() {
			let e = E.panels;
			return b`
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
          opacity: var(${Ti.PLAY_MODE_ICON_PROGRESS_CSS_VAR}, 1);
        }

        .toolbar-play-mode-icon--vaadin {
          opacity: calc(1 - var(${Ti.PLAY_MODE_ICON_PROGRESS_CSS_VAR}, 1));
        }
      </style>
      <div class="flex pe-1" id="mode-panel-icon-container" ?hidden="${O.activeMode === "play"}">
        ${O.activeMode ? this.renderPanelList(e, O.activeMode) : _}
      </div>
      <div class="flex pe-1" id="mode-icon-container">${this.renderCopilotModeButtons()}</div>
      <div class="flex" id="common-mode-panel-icon-container">
        <vaadin-button
          aria-label="DevTools"
          class="max-w-6 min-w-0 relative"
          id="devtools-button"
          theme="icon tertiary toolbar">
          <vaadin-icon .svg="${y.moreVert}"></vaadin-icon>
          <vaadin-tooltip slot="tooltip" text="Open menu"></vaadin-tooltip>
        </vaadin-button>
        <vaadin-popover
          @opened-changed="${(e) => {
				this.expandModeHandler.devToolsPopoverOpenClosedChanged(e.detail.value), e.detail.value && !this.devToolsDataLoaded && (this.devToolsDataLoading = !0, import("./copilot-devtools-B2ntrV2F.js").then(() => {
					be(), E.restorePanelsFromStorage(), ve(), Me(), this.devToolsDataLoading = !1, this.devToolsDataLoaded = !0;
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
		renderPanelList(e, t) {
			return b`
      ${nt(e.filter((e) => e.experimental?.enabled() !== !1).filter((e) => e.toolbarOptions?.allowedModesWithOrder?.[t] !== void 0).sort((e, n) => e.toolbarOptions.allowedModesWithOrder[t] - n.toolbarOptions.allowedModesWithOrder[t]), (e) => e.tag, (e) => this.renderPanelIcon(e))}
    `;
		}
		renderCopilotModeButtons() {
			return b`
      <vaadin-radio-group
        accessible-name="Mode"
        theme="toolbar"
        .value="${O.activeMode}"
        @change="${(e) => {
				let t = e.target.value;
				O.setActiveMode(t, !0), Xe();
			}}">
        ${nt(this.modeRadioButtonItems, (e) => e.value, (e) => b`
            <vaadin-radio-button data-mode="${e.value}" .value="${e.value}" .id="${e.value}-mode-radio-btn">
              <label slot="label">${this.renderModeButtonIcon(e)}</label>
            </vaadin-radio-button>
          `)}
      </vaadin-radio-group>
      ${nt(this.modeRadioButtonItems, (e) => e.value, (e) => b`
          <vaadin-tooltip for="${e.value}-mode-radio-btn" text="${e.label} Mode"></vaadin-tooltip>
        `)}
    `;
		}
		renderModeButtonIcon(e) {
			return e.value === "play" ? b`
      <span aria-hidden="true" class="toolbar-play-mode-icon-stack">
        <vaadin-icon class="toolbar-play-mode-icon--play" .svg="${y[e.icon]}"></vaadin-icon>
        <vaadin-icon class="toolbar-play-mode-icon--vaadin" .svg="${y.vaadin}"></vaadin-icon>
      </span>
    ` : b`<vaadin-icon .svg="${y[e.icon]}"></vaadin-icon>`;
		}
		renderPanelIcon(e) {
			let t = this.getButtonId(e);
			return b`
      <vaadin-button
        aria-expanded="${E.isOpenedPanel(e.tag)}"
        aria-label="${e.header}"
        class="panel-icon-button relative"
        data-panel-tag="${e.tag}"
        id="${t}"
        theme="icon tertiary toolbar"
        @click=${(t) => {
				if (E.isOpenedPanel(e.tag)) {
					let t = (this.getRootNode().querySelector("copilot-panel-manager")?.getDialogByPanelTag(e.tag))?.querySelector(e.tag);
					t?.requestClose ? t.requestClose(() => E.closePanel(e.tag)) : E.closePanel(e.tag);
				} else E.attentionRequiredPanelTag === e.tag && E.clearAttention(), E.openPanel(e.tag);
			}}>
        <vaadin-icon .svg="${y[e.toolbarOptions.iconKey]}"></vaadin-icon>
        <vaadin-tooltip
          slot="tooltip"
          text="${e.header}${E.attentionRequiredPanelTag === e.tag ? " – Attention Required" : ""}"></vaadin-tooltip>
        ${E.attentionRequiredPanelTag === e.tag ? b`<span
                aria-hidden="true"
                class="absolute animate-ping bg-amber-11 end-0.5 rounded-full size-2 top-0.5"></span>
              <span
                aria-hidden="true"
                class="absolute bg-amber-11 border border-gray-1 dark:border-gray-5 box-border end-0.5 rounded-full size-2 top-0.5"></span>` : _}
      </vaadin-button>
    `;
		}
		getButtonId(e) {
			return `${e.tag}-toolbar-btn`;
		}
		getOrderByTag(e) {
			let t = /* @__PURE__ */ new Map();
			return [...e.querySelectorAll("[data-panel-tag]")].forEach((e, n) => {
				t.set(e.dataset.panelTag, n);
			}), t;
		}
		queryModeRadioButtons() {
			return Array.from(this.querySelectorAll("vaadin-radio-button"));
		}
		queryRadioButtonForMode(e) {
			return this.querySelector(`vaadin-radio-button#${e}-mode-radio-btn`);
		}
	}, Ti = wi, wi.TRANSITION_STATUS_ATTR_KEY = "transition-status", wi.PLAY_MODE_ICON_PROGRESS_CSS_VAR = "--play-mode-icon-progress", wi), w([le("#mode-panel-icon-container")], Ei.prototype, "modePanelIconContainer", void 0), w([le("#devtools-popover")], Ei.prototype, "devToolsPopover", void 0), w([le("vaadin-radio-group")], Ei.prototype, "radioGroup", void 0), w([T()], Ei.prototype, "modeRadioButtonItems", void 0), w([T()], Ei.prototype, "devToolsDataLoading", void 0), w([T()], Ei.prototype, "devToolsDataLoaded", void 0), Ei = Ti = w([ue("copilot-toolbar")], Ei);
}));
//#endregion
//#region frontend/copilot/project-info-data.ts
function Oi() {
	let e = [];
	O.userInfo?.vaadiner && e.push({
		name: "Vaadin Employee",
		value: "true",
		booleanInfo: { ariaLabel: "Yes" }
	});
	let t = [
		...k.serverVersions.map((e) => ({
			name: e.name,
			value: e.version
		})),
		...e,
		...k.clientVersions.map((e) => ({
			name: e.name,
			value: e.version
		}))
	];
	return t.forEach((e) => {
		e.name === "Frontend Hotswap" && (e.value === "true" ? e.booleanInfo = {
			ariaLabel: "Enabled",
			text: "Vite"
		} : e.value === "false" && (e.booleanInfo = {
			ariaLabel: "Disabled",
			text: "Pre-Built Bundle"
		}));
	}), k.springSecurityEnabled && t.push({
		name: "Spring Security",
		value: "true",
		booleanInfo: { ariaLabel: "Enabled" }
	}), k.springJpaDataEnabled && t.push({
		name: "Spring Data JPA",
		value: "true",
		booleanInfo: { ariaLabel: "Enabled" }
	}), t.push(...Ai()), ki(t), t;
}
function ki(e) {
	for (let t of e) t.value === "true" ? (t.value = !0, t.booleanInfo ? t.booleanInfo.ariaLabel || (t.booleanInfo.ariaLabel = "Enabled") : t.booleanInfo = { ariaLabel: "Enabled" }) : t.value === "false" && (t.value = !1, t.booleanInfo ? t.booleanInfo.ariaLabel || (t.booleanInfo.ariaLabel = "Disabled") : t.booleanInfo = { ariaLabel: "Disabled" });
}
function Ai() {
	let e = [], t = qe(), n = Ni(O.idePluginState), r = Pi(t);
	return e.push({
		name: "Java Hotswap",
		value: t === "success",
		booleanInfo: {
			ariaLabel: t === "success" ? "Enabled" : "Disabled",
			text: r
		}
	}), Mi() !== "unsupported" && e.push({
		name: "IDE Plugin",
		value: Mi() === "success",
		booleanInfo: {
			ariaLabel: Mi() === "success" ? "Installed" : "Not Installed",
			text: n
		}
	}), e;
}
function ji(e) {
	let t = O.projectInfoEntries;
	if (!t) throw Error("Unable to load project info entries");
	let n = t.map((e) => ({
		key: e.name,
		value: e.value
	})).filter((e) => e.key !== "Live reload").filter((e) => !e.key.startsWith("Vaadin Emplo")).filter((e) => e.key !== "Development Workflow").map((e) => {
		let { key: t } = e, { value: n } = e;
		if (t === "IDE Plugin") n = Ni(O.idePluginState) ?? "false";
		else if (t === "Java Hotswap") {
			let e = k.jdkInfo?.jrebel, t = qe();
			n = e && t === "success" ? "JRebel is in use" : Pi(t);
		}
		return t === "Frontend Hotswap" && (n === "true" || n === !0 ? n = "Vite" : (n === "false" || n === !1) && (n = "Pre-Built Bundle")), `${t}: ${n}`;
	}).join("\n");
	return e && We({
		type: r.INFORMATION,
		message: "Environment information copied to clipboard"
	}), n.trim();
}
function Mi() {
	return O.idePluginState !== void 0 && !O.idePluginState.active ? "warning" : "success";
}
function Ni(e) {
	if (Mi() !== "success") return "Not installed";
	if (e?.version) {
		let t = null;
		return e?.ide && (e?.ide === "intellij" ? t = "IntelliJ" : e?.ide === "vscode" ? t = "VS Code" : e?.ide === "eclipse" && (t = "Eclipse")), t ? `${e?.version} ${t}` : e?.version;
	}
	return "Not installed";
}
function Pi(e) {
	return e === "success" ? "Java Hotswap is enabled" : e === "error" ? "Hotswap is partially enabled" : "Hotswap is disabled";
}
var Fi = t((() => {
	d(), Ke(), we(), Ye(), Ue(), p(), _e(), x(() => [
		k.serverVersions,
		k.clientVersions,
		O.userInfo,
		k.springSecurityEnabled,
		k.springJpaDataEnabled,
		k.jdkInfo,
		O.idePluginState
	], () => {
		let e = Oi();
		O.setProjectInfoEntries(e);
	}, { fireImmediately: !0 }), D.on("system-info-with-callback", (e) => {
		e.detail.callback(ji(e.detail.notify));
	});
})), Ii, Li = t((() => {
	v(), me(), we(), de(), Ke(), De(), Ct(), g(), fe(), Ii = class extends pe {
		constructor(...e) {
			super(...e), this.renderDialog = () => b`
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
  `, this.renderFooter = () => b`
    <vaadin-button @click=${this.cancelClickListener}>Cancel</vaadin-button>
    <vaadin-button theme="primary" @click=${this.loginClickListener}>Login</vaadin-button>
  `, this.tryAcquireLicense = () => {
				let e = k.serverVersions.find((e) => e.name === "Vaadin")?.version;
				window.Vaadin.devTools.downloadLicense({
					name: "vaadin-copilot",
					version: e
				});
			};
		}
		createRenderRoot() {
			return this;
		}
		render() {
			return b` <vaadin-dialog
      @opened-changed="${(e) => {
				let t = e.detail.value;
				O.setLoginCheckActive(t);
			}}"
      .opened="${O.loginCheckActive}"
      ${bt(() => b`
          <h2 class="font-bold me-auto my-0 text-xs truncate uppercase">Unlock all AI features</h2>
          <vaadin-button aria-label="Close" @click="${() => {}}" theme="icon tertiary">
            <vaadin-icon .svg="${y.close}"></vaadin-icon>
            <vaadin-tooltip slot="tooltip" text="Close"></vaadin-tooltip>
          </vaadin-button>
        `)}
      ${yt(this.renderDialog, [])}
      ${xt(this.renderFooter, [])}>
    </vaadin-dialog>`;
		}
		cancelClickListener(e) {
			O.setLoginCheckActive(!1);
		}
		loginClickListener() {
			Ee("license-acquired"), this.tryAcquireLicense();
		}
	}, Ii = w([ue("copilot-login-check")], Ii);
})), Ri = /* @__PURE__ */ n(((e, t) => {
	var n = "2.0.0", r = 256;
	t.exports = {
		MAX_LENGTH: r,
		MAX_SAFE_COMPONENT_LENGTH: 16,
		MAX_SAFE_BUILD_LENGTH: r - 6,
		MAX_SAFE_INTEGER: 2 ** 53 - 1 || 9007199254740991,
		RELEASE_TYPES: [
			"major",
			"premajor",
			"minor",
			"preminor",
			"patch",
			"prepatch",
			"prerelease"
		],
		SEMVER_SPEC_VERSION: n,
		FLAG_INCLUDE_PRERELEASE: 1,
		FLAG_LOOSE: 2
	};
})), zi = /* @__PURE__ */ n(((e, t) => {
	t.exports = typeof process == "object" && process.env && process.env.NODE_DEBUG && /\bsemver\b/i.test(process.env.NODE_DEBUG) ? (...e) => console.error("SEMVER", ...e) : () => {};
})), Bi = /* @__PURE__ */ n(((e, t) => {
	var { MAX_SAFE_COMPONENT_LENGTH: n, MAX_SAFE_BUILD_LENGTH: r, MAX_LENGTH: i } = Ri(), a = zi();
	e = t.exports = {};
	var o = e.re = [], s = e.safeRe = [], c = e.src = [], l = e.safeSrc = [], u = e.t = {}, d = 0, f = "[a-zA-Z0-9-]", p = [
		["\\s", 1],
		["\\d", i],
		[f, r]
	], m = (e) => {
		for (let [t, n] of p) e = e.split(`${t}*`).join(`${t}{0,${n}}`).split(`${t}+`).join(`${t}{1,${n}}`);
		return e;
	}, h = (e, t, n) => {
		let r = m(t), i = d++;
		a(e, i, t), u[e] = i, c[i] = t, l[i] = r, o[i] = new RegExp(t, n ? "g" : void 0), s[i] = new RegExp(r, n ? "g" : void 0);
	};
	h("NUMERICIDENTIFIER", "0|[1-9]\\d*"), h("NUMERICIDENTIFIERLOOSE", "\\d+"), h("NONNUMERICIDENTIFIER", `\\d*[a-zA-Z-]${f}*`), h("MAINVERSION", `(${c[u.NUMERICIDENTIFIER]})\\.(${c[u.NUMERICIDENTIFIER]})\\.(${c[u.NUMERICIDENTIFIER]})`), h("MAINVERSIONLOOSE", `(${c[u.NUMERICIDENTIFIERLOOSE]})\\.(${c[u.NUMERICIDENTIFIERLOOSE]})\\.(${c[u.NUMERICIDENTIFIERLOOSE]})`), h("PRERELEASEIDENTIFIER", `(?:${c[u.NONNUMERICIDENTIFIER]}|${c[u.NUMERICIDENTIFIER]})`), h("PRERELEASEIDENTIFIERLOOSE", `(?:${c[u.NONNUMERICIDENTIFIER]}|${c[u.NUMERICIDENTIFIERLOOSE]})`), h("PRERELEASE", `(?:-(${c[u.PRERELEASEIDENTIFIER]}(?:\\.${c[u.PRERELEASEIDENTIFIER]})*))`), h("PRERELEASELOOSE", `(?:-?(${c[u.PRERELEASEIDENTIFIERLOOSE]}(?:\\.${c[u.PRERELEASEIDENTIFIERLOOSE]})*))`), h("BUILDIDENTIFIER", `${f}+`), h("BUILD", `(?:\\+(${c[u.BUILDIDENTIFIER]}(?:\\.${c[u.BUILDIDENTIFIER]})*))`), h("FULLPLAIN", `v?${c[u.MAINVERSION]}${c[u.PRERELEASE]}?${c[u.BUILD]}?`), h("FULL", `^${c[u.FULLPLAIN]}$`), h("LOOSEPLAIN", `[v=\\s]*${c[u.MAINVERSIONLOOSE]}${c[u.PRERELEASELOOSE]}?${c[u.BUILD]}?`), h("LOOSE", `^${c[u.LOOSEPLAIN]}$`), h("GTLT", "((?:<|>)?=?)"), h("XRANGEIDENTIFIERLOOSE", `${c[u.NUMERICIDENTIFIERLOOSE]}|x|X|\\*`), h("XRANGEIDENTIFIER", `${c[u.NUMERICIDENTIFIER]}|x|X|\\*`), h("XRANGEPLAIN", `[v=\\s]*(${c[u.XRANGEIDENTIFIER]})(?:\\.(${c[u.XRANGEIDENTIFIER]})(?:\\.(${c[u.XRANGEIDENTIFIER]})(?:${c[u.PRERELEASE]})?${c[u.BUILD]}?)?)?`), h("XRANGEPLAINLOOSE", `[v=\\s]*(${c[u.XRANGEIDENTIFIERLOOSE]})(?:\\.(${c[u.XRANGEIDENTIFIERLOOSE]})(?:\\.(${c[u.XRANGEIDENTIFIERLOOSE]})(?:${c[u.PRERELEASELOOSE]})?${c[u.BUILD]}?)?)?`), h("XRANGE", `^${c[u.GTLT]}\\s*${c[u.XRANGEPLAIN]}$`), h("XRANGELOOSE", `^${c[u.GTLT]}\\s*${c[u.XRANGEPLAINLOOSE]}$`), h("COERCEPLAIN", `(^|[^\\d])(\\d{1,${n}})(?:\\.(\\d{1,${n}}))?(?:\\.(\\d{1,${n}}))?`), h("COERCE", `${c[u.COERCEPLAIN]}(?:$|[^\\d])`), h("COERCEFULL", c[u.COERCEPLAIN] + `(?:${c[u.PRERELEASE]})?(?:${c[u.BUILD]})?(?:$|[^\\d])`), h("COERCERTL", c[u.COERCE], !0), h("COERCERTLFULL", c[u.COERCEFULL], !0), h("LONETILDE", "(?:~>?)"), h("TILDETRIM", `(\\s*)${c[u.LONETILDE]}\\s+`, !0), e.tildeTrimReplace = "$1~", h("TILDE", `^${c[u.LONETILDE]}${c[u.XRANGEPLAIN]}$`), h("TILDELOOSE", `^${c[u.LONETILDE]}${c[u.XRANGEPLAINLOOSE]}$`), h("LONECARET", "(?:\\^)"), h("CARETTRIM", `(\\s*)${c[u.LONECARET]}\\s+`, !0), e.caretTrimReplace = "$1^", h("CARET", `^${c[u.LONECARET]}${c[u.XRANGEPLAIN]}$`), h("CARETLOOSE", `^${c[u.LONECARET]}${c[u.XRANGEPLAINLOOSE]}$`), h("COMPARATORLOOSE", `^${c[u.GTLT]}\\s*(${c[u.LOOSEPLAIN]})$|^$`), h("COMPARATOR", `^${c[u.GTLT]}\\s*(${c[u.FULLPLAIN]})$|^$`), h("COMPARATORTRIM", `(\\s*)${c[u.GTLT]}\\s*(${c[u.LOOSEPLAIN]}|${c[u.XRANGEPLAIN]})`, !0), e.comparatorTrimReplace = "$1$2$3", h("HYPHENRANGE", `^\\s*(${c[u.XRANGEPLAIN]})\\s+-\\s+(${c[u.XRANGEPLAIN]})\\s*$`), h("HYPHENRANGELOOSE", `^\\s*(${c[u.XRANGEPLAINLOOSE]})\\s+-\\s+(${c[u.XRANGEPLAINLOOSE]})\\s*$`), h("STAR", "(<|>)?=?\\s*\\*"), h("GTE0", "^\\s*>=\\s*0\\.0\\.0\\s*$"), h("GTE0PRE", "^\\s*>=\\s*0\\.0\\.0-0\\s*$");
})), Vi = /* @__PURE__ */ n(((e, t) => {
	var n = Object.freeze({ loose: !0 }), r = Object.freeze({});
	t.exports = (e) => e ? typeof e == "object" ? e : n : r;
})), Hi = /* @__PURE__ */ n(((e, t) => {
	var n = /^[0-9]+$/, r = (e, t) => {
		if (typeof e == "number" && typeof t == "number") return e === t ? 0 : e < t ? -1 : 1;
		let r = n.test(e), i = n.test(t);
		return r && i && (e = +e, t = +t), e === t ? 0 : r && !i ? -1 : i && !r ? 1 : e < t ? -1 : 1;
	};
	t.exports = {
		compareIdentifiers: r,
		rcompareIdentifiers: (e, t) => r(t, e)
	};
})), Q = /* @__PURE__ */ n(((e, t) => {
	var n = zi(), { MAX_LENGTH: r, MAX_SAFE_INTEGER: i } = Ri(), { safeRe: a, t: o } = Bi(), s = Vi(), { compareIdentifiers: c } = Hi();
	t.exports = class e {
		constructor(t, c) {
			if (c = s(c), t instanceof e) {
				if (t.loose === !!c.loose && t.includePrerelease === !!c.includePrerelease) return t;
				t = t.version;
			} else if (typeof t != "string") throw TypeError(`Invalid version. Must be a string. Got type "${typeof t}".`);
			if (t.length > r) throw TypeError(`version is longer than ${r} characters`);
			n("SemVer", t, c), this.options = c, this.loose = !!c.loose, this.includePrerelease = !!c.includePrerelease;
			let l = t.trim().match(c.loose ? a[o.LOOSE] : a[o.FULL]);
			if (!l) throw TypeError(`Invalid Version: ${t}`);
			if (this.raw = t, this.major = +l[1], this.minor = +l[2], this.patch = +l[3], this.major > i || this.major < 0) throw TypeError("Invalid major version");
			if (this.minor > i || this.minor < 0) throw TypeError("Invalid minor version");
			if (this.patch > i || this.patch < 0) throw TypeError("Invalid patch version");
			l[4] ? this.prerelease = l[4].split(".").map((e) => {
				if (/^[0-9]+$/.test(e)) {
					let t = +e;
					if (t >= 0 && t < i) return t;
				}
				return e;
			}) : this.prerelease = [], this.build = l[5] ? l[5].split(".") : [], this.format();
		}
		format() {
			return this.version = `${this.major}.${this.minor}.${this.patch}`, this.prerelease.length && (this.version += `-${this.prerelease.join(".")}`), this.version;
		}
		toString() {
			return this.version;
		}
		compare(t) {
			if (n("SemVer.compare", this.version, this.options, t), !(t instanceof e)) {
				if (typeof t == "string" && t === this.version) return 0;
				t = new e(t, this.options);
			}
			return t.version === this.version ? 0 : this.compareMain(t) || this.comparePre(t);
		}
		compareMain(t) {
			return t instanceof e || (t = new e(t, this.options)), this.major < t.major ? -1 : this.major > t.major ? 1 : this.minor < t.minor ? -1 : this.minor > t.minor ? 1 : this.patch < t.patch ? -1 : +(this.patch > t.patch);
		}
		comparePre(t) {
			if (t instanceof e || (t = new e(t, this.options)), this.prerelease.length && !t.prerelease.length) return -1;
			if (!this.prerelease.length && t.prerelease.length) return 1;
			if (!this.prerelease.length && !t.prerelease.length) return 0;
			let r = 0;
			do {
				let e = this.prerelease[r], i = t.prerelease[r];
				if (n("prerelease compare", r, e, i), e === void 0 && i === void 0) return 0;
				if (i === void 0) return 1;
				if (e === void 0) return -1;
				if (e === i) continue;
				return c(e, i);
			} while (++r);
		}
		compareBuild(t) {
			t instanceof e || (t = new e(t, this.options));
			let r = 0;
			do {
				let e = this.build[r], i = t.build[r];
				if (n("build compare", r, e, i), e === void 0 && i === void 0) return 0;
				if (i === void 0) return 1;
				if (e === void 0) return -1;
				if (e === i) continue;
				return c(e, i);
			} while (++r);
		}
		inc(e, t, n) {
			if (e.startsWith("pre")) {
				if (!t && n === !1) throw Error("invalid increment argument: identifier is empty");
				if (t) {
					let e = `-${t}`.match(this.options.loose ? a[o.PRERELEASELOOSE] : a[o.PRERELEASE]);
					if (!e || e[1] !== t) throw Error(`invalid identifier: ${t}`);
				}
			}
			switch (e) {
				case "premajor":
					this.prerelease.length = 0, this.patch = 0, this.minor = 0, this.major++, this.inc("pre", t, n);
					break;
				case "preminor":
					this.prerelease.length = 0, this.patch = 0, this.minor++, this.inc("pre", t, n);
					break;
				case "prepatch":
					this.prerelease.length = 0, this.inc("patch", t, n), this.inc("pre", t, n);
					break;
				case "prerelease":
					this.prerelease.length === 0 && this.inc("patch", t, n), this.inc("pre", t, n);
					break;
				case "release":
					if (this.prerelease.length === 0) throw Error(`version ${this.raw} is not a prerelease`);
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
				case "pre": {
					let e = +!!Number(n);
					if (this.prerelease.length === 0) this.prerelease = [e];
					else {
						let r = this.prerelease.length;
						for (; --r >= 0;) typeof this.prerelease[r] == "number" && (this.prerelease[r]++, r = -2);
						if (r === -1) {
							if (t === this.prerelease.join(".") && n === !1) throw Error("invalid increment argument: identifier already exists");
							this.prerelease.push(e);
						}
					}
					if (t) {
						let r = [t, e];
						n === !1 && (r = [t]), c(this.prerelease[0], t) === 0 ? isNaN(this.prerelease[1]) && (this.prerelease = r) : this.prerelease = r;
					}
					break;
				}
				default: throw Error(`invalid increment argument: ${e}`);
			}
			return this.raw = this.format(), this.build.length && (this.raw += `+${this.build.join(".")}`), this;
		}
	};
})), Ui = /* @__PURE__ */ n(((e, t) => {
	var n = Q();
	t.exports = (e, t, r = !1) => {
		if (e instanceof n) return e;
		try {
			return new n(e, t);
		} catch (e) {
			if (!r) return null;
			throw e;
		}
	};
})), Wi = /* @__PURE__ */ n(((e, t) => {
	var n = Ui();
	t.exports = (e, t) => {
		let r = n(e, t);
		return r ? r.version : null;
	};
})), Gi = /* @__PURE__ */ n(((e, t) => {
	var n = Ui();
	t.exports = (e, t) => {
		let r = n(e.trim().replace(/^[=v]+/, ""), t);
		return r ? r.version : null;
	};
})), Ki = /* @__PURE__ */ n(((e, t) => {
	var n = Q();
	t.exports = (e, t, r, i, a) => {
		typeof r == "string" && (a = i, i = r, r = void 0);
		try {
			return new n(e instanceof n ? e.version : e, r).inc(t, i, a).version;
		} catch {
			return null;
		}
	};
})), qi = /* @__PURE__ */ n(((e, t) => {
	var n = Ui();
	t.exports = (e, t) => {
		let r = n(e, null, !0), i = n(t, null, !0), a = r.compare(i);
		if (a === 0) return null;
		let o = a > 0, s = o ? r : i, c = o ? i : r, l = !!s.prerelease.length;
		if (c.prerelease.length && !l) {
			if (!c.patch && !c.minor) return "major";
			if (c.compareMain(s) === 0) return c.minor && !c.patch ? "minor" : "patch";
		}
		let u = l ? "pre" : "";
		return r.major === i.major ? r.minor === i.minor ? r.patch === i.patch ? "prerelease" : u + "patch" : u + "minor" : u + "major";
	};
})), Ji = /* @__PURE__ */ n(((e, t) => {
	var n = Q();
	t.exports = (e, t) => new n(e, t).major;
})), Yi = /* @__PURE__ */ n(((e, t) => {
	var n = Q();
	t.exports = (e, t) => new n(e, t).minor;
})), Xi = /* @__PURE__ */ n(((e, t) => {
	var n = Q();
	t.exports = (e, t) => new n(e, t).patch;
})), Zi = /* @__PURE__ */ n(((e, t) => {
	var n = Ui();
	t.exports = (e, t) => {
		let r = n(e, t);
		return r && r.prerelease.length ? r.prerelease : null;
	};
})), Qi = /* @__PURE__ */ n(((e, t) => {
	var n = Q();
	t.exports = (e, t, r) => new n(e, r).compare(new n(t, r));
})), $i = /* @__PURE__ */ n(((e, t) => {
	var n = Qi();
	t.exports = (e, t, r) => n(t, e, r);
})), ea = /* @__PURE__ */ n(((e, t) => {
	var n = Qi();
	t.exports = (e, t) => n(e, t, !0);
})), ta = /* @__PURE__ */ n(((e, t) => {
	var n = Q();
	t.exports = (e, t, r) => {
		let i = new n(e, r), a = new n(t, r);
		return i.compare(a) || i.compareBuild(a);
	};
})), na = /* @__PURE__ */ n(((e, t) => {
	var n = ta();
	t.exports = (e, t) => e.sort((e, r) => n(e, r, t));
})), ra = /* @__PURE__ */ n(((e, t) => {
	var n = ta();
	t.exports = (e, t) => e.sort((e, r) => n(r, e, t));
})), ia = /* @__PURE__ */ n(((e, t) => {
	var n = Qi();
	t.exports = (e, t, r) => n(e, t, r) > 0;
})), aa = /* @__PURE__ */ n(((e, t) => {
	var n = Qi();
	t.exports = (e, t, r) => n(e, t, r) < 0;
})), oa = /* @__PURE__ */ n(((e, t) => {
	var n = Qi();
	t.exports = (e, t, r) => n(e, t, r) === 0;
})), sa = /* @__PURE__ */ n(((e, t) => {
	var n = Qi();
	t.exports = (e, t, r) => n(e, t, r) !== 0;
})), ca = /* @__PURE__ */ n(((e, t) => {
	var n = Qi();
	t.exports = (e, t, r) => n(e, t, r) >= 0;
})), la = /* @__PURE__ */ n(((e, t) => {
	var n = Qi();
	t.exports = (e, t, r) => n(e, t, r) <= 0;
})), ua = /* @__PURE__ */ n(((e, t) => {
	var n = oa(), r = sa(), i = ia(), a = ca(), o = aa(), s = la();
	t.exports = (e, t, c, l) => {
		switch (t) {
			case "===": return typeof e == "object" && (e = e.version), typeof c == "object" && (c = c.version), e === c;
			case "!==": return typeof e == "object" && (e = e.version), typeof c == "object" && (c = c.version), e !== c;
			case "":
			case "=":
			case "==": return n(e, c, l);
			case "!=": return r(e, c, l);
			case ">": return i(e, c, l);
			case ">=": return a(e, c, l);
			case "<": return o(e, c, l);
			case "<=": return s(e, c, l);
			default: throw TypeError(`Invalid operator: ${t}`);
		}
	};
})), da = /* @__PURE__ */ n(((e, t) => {
	var n = Q(), r = Ui(), { safeRe: i, t: a } = Bi();
	t.exports = (e, t) => {
		if (e instanceof n) return e;
		if (typeof e == "number" && (e = String(e)), typeof e != "string") return null;
		t ||= {};
		let o = null;
		if (!t.rtl) o = e.match(t.includePrerelease ? i[a.COERCEFULL] : i[a.COERCE]);
		else {
			let n = t.includePrerelease ? i[a.COERCERTLFULL] : i[a.COERCERTL], r;
			for (; (r = n.exec(e)) && (!o || o.index + o[0].length !== e.length);) (!o || r.index + r[0].length !== o.index + o[0].length) && (o = r), n.lastIndex = r.index + r[1].length + r[2].length;
			n.lastIndex = -1;
		}
		if (o === null) return null;
		let s = o[2];
		return r(`${s}.${o[3] || "0"}.${o[4] || "0"}${t.includePrerelease && o[5] ? `-${o[5]}` : ""}${t.includePrerelease && o[6] ? `+${o[6]}` : ""}`, t);
	};
})), fa = /* @__PURE__ */ n(((e, t) => {
	t.exports = class {
		constructor() {
			this.max = 1e3, this.map = /* @__PURE__ */ new Map();
		}
		get(e) {
			let t = this.map.get(e);
			if (t !== void 0) return this.map.delete(e), this.map.set(e, t), t;
		}
		delete(e) {
			return this.map.delete(e);
		}
		set(e, t) {
			if (!this.delete(e) && t !== void 0) {
				if (this.map.size >= this.max) {
					let e = this.map.keys().next().value;
					this.delete(e);
				}
				this.map.set(e, t);
			}
			return this;
		}
	};
})), $ = /* @__PURE__ */ n(((e, t) => {
	var n = /\s+/g;
	t.exports = class e {
		constructor(t, r) {
			if (r = i(r), t instanceof e) return t.loose === !!r.loose && t.includePrerelease === !!r.includePrerelease ? t : new e(t.raw, r);
			if (t instanceof a) return this.raw = t.value, this.set = [[t]], this.formatted = void 0, this;
			if (this.options = r, this.loose = !!r.loose, this.includePrerelease = !!r.includePrerelease, this.raw = t.trim().replace(n, " "), this.set = this.raw.split("||").map((e) => this.parseRange(e.trim())).filter((e) => e.length), !this.set.length) throw TypeError(`Invalid SemVer Range: ${this.raw}`);
			if (this.set.length > 1) {
				let e = this.set[0];
				if (this.set = this.set.filter((e) => !h(e[0])), this.set.length === 0) this.set = [e];
				else if (this.set.length > 1) {
					for (let e of this.set) if (e.length === 1 && g(e[0])) {
						this.set = [e];
						break;
					}
				}
			}
			this.formatted = void 0;
		}
		get range() {
			if (this.formatted === void 0) {
				this.formatted = "";
				for (let e = 0; e < this.set.length; e++) {
					e > 0 && (this.formatted += "||");
					let t = this.set[e];
					for (let e = 0; e < t.length; e++) e > 0 && (this.formatted += " "), this.formatted += t[e].toString().trim();
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
		parseRange(e) {
			let t = ((this.options.includePrerelease && p) | (this.options.loose && m)) + ":" + e, n = r.get(t);
			if (n) return n;
			let i = this.options.loose, s = i ? c[l.HYPHENRANGELOOSE] : c[l.HYPHENRANGE];
			e = e.replace(s, C(this.options.includePrerelease)), o("hyphen replace", e), e = e.replace(c[l.COMPARATORTRIM], u), o("comparator trim", e), e = e.replace(c[l.TILDETRIM], d), o("tilde trim", e), e = e.replace(c[l.CARETTRIM], f), o("caret trim", e);
			let g = e.split(" ").map((e) => v(e, this.options)).join(" ").split(/\s+/).map((e) => ie(e, this.options));
			i && (g = g.filter((e) => (o("loose invalid filter", e, this.options), !!e.match(c[l.COMPARATORLOOSE])))), o("range list", g);
			let _ = /* @__PURE__ */ new Map(), y = g.map((e) => new a(e, this.options));
			for (let e of y) {
				if (h(e)) return [e];
				_.set(e.value, e);
			}
			_.size > 1 && _.has("") && _.delete("");
			let b = [..._.values()];
			return r.set(t, b), b;
		}
		intersects(t, n) {
			if (!(t instanceof e)) throw TypeError("a Range is required");
			return this.set.some((e) => _(e, n) && t.set.some((t) => _(t, n) && e.every((e) => t.every((t) => e.intersects(t, n)))));
		}
		test(e) {
			if (!e) return !1;
			if (typeof e == "string") try {
				e = new s(e, this.options);
			} catch {
				return !1;
			}
			for (let t = 0; t < this.set.length; t++) if (ae(this.set[t], e, this.options)) return !0;
			return !1;
		}
	};
	var r = new (fa())(), i = Vi(), a = pa(), o = zi(), s = Q(), { safeRe: c, t: l, comparatorTrimReplace: u, tildeTrimReplace: d, caretTrimReplace: f } = Bi(), { FLAG_INCLUDE_PRERELEASE: p, FLAG_LOOSE: m } = Ri(), h = (e) => e.value === "<0.0.0-0", g = (e) => e.value === "", _ = (e, t) => {
		let n = !0, r = e.slice(), i = r.pop();
		for (; n && r.length;) n = r.every((e) => i.intersects(e, t)), i = r.pop();
		return n;
	}, v = (e, t) => (e = e.replace(c[l.BUILD], ""), o("comp", e, t), e = ee(e, t), o("caret", e), e = b(e, t), o("tildes", e), e = ne(e, t), o("xrange", e), e = re(e, t), o("stars", e), e), y = (e) => !e || e.toLowerCase() === "x" || e === "*", b = (e, t) => e.trim().split(/\s+/).map((e) => x(e, t)).join(" "), x = (e, t) => {
		let n = t.loose ? c[l.TILDELOOSE] : c[l.TILDE];
		return e.replace(n, (t, n, r, i, a) => {
			o("tilde", e, t, n, r, i, a);
			let s;
			return y(n) ? s = "" : y(r) ? s = `>=${n}.0.0 <${+n + 1}.0.0-0` : y(i) ? s = `>=${n}.${r}.0 <${n}.${+r + 1}.0-0` : a ? (o("replaceTilde pr", a), s = `>=${n}.${r}.${i}-${a} <${n}.${+r + 1}.0-0`) : s = `>=${n}.${r}.${i} <${n}.${+r + 1}.0-0`, o("tilde return", s), s;
		});
	}, ee = (e, t) => e.trim().split(/\s+/).map((e) => te(e, t)).join(" "), te = (e, t) => {
		o("caret", e, t);
		let n = t.loose ? c[l.CARETLOOSE] : c[l.CARET], r = t.includePrerelease ? "-0" : "";
		return e.replace(n, (t, n, i, a, s) => {
			o("caret", e, t, n, i, a, s);
			let c;
			return y(n) ? c = "" : y(i) ? c = `>=${n}.0.0${r} <${+n + 1}.0.0-0` : y(a) ? c = n === "0" ? `>=${n}.${i}.0${r} <${n}.${+i + 1}.0-0` : `>=${n}.${i}.0${r} <${+n + 1}.0.0-0` : s ? (o("replaceCaret pr", s), c = n === "0" ? i === "0" ? `>=${n}.${i}.${a}-${s} <${n}.${i}.${+a + 1}-0` : `>=${n}.${i}.${a}-${s} <${n}.${+i + 1}.0-0` : `>=${n}.${i}.${a}-${s} <${+n + 1}.0.0-0`) : (o("no pr"), c = n === "0" ? i === "0" ? `>=${n}.${i}.${a}${r} <${n}.${i}.${+a + 1}-0` : `>=${n}.${i}.${a}${r} <${n}.${+i + 1}.0-0` : `>=${n}.${i}.${a} <${+n + 1}.0.0-0`), o("caret return", c), c;
		});
	}, ne = (e, t) => (o("replaceXRanges", e, t), e.split(/\s+/).map((e) => S(e, t)).join(" ")), S = (e, t) => {
		e = e.trim();
		let n = t.loose ? c[l.XRANGELOOSE] : c[l.XRANGE];
		return e.replace(n, (n, r, i, a, s, c) => {
			o("xRange", e, n, r, i, a, s, c);
			let l = y(i), u = l || y(a), d = u || y(s), f = d;
			return r === "=" && f && (r = ""), c = t.includePrerelease ? "-0" : "", l ? n = r === ">" || r === "<" ? "<0.0.0-0" : "*" : r && f ? (u && (a = 0), s = 0, r === ">" ? (r = ">=", u ? (i = +i + 1, a = 0, s = 0) : (a = +a + 1, s = 0)) : r === "<=" && (r = "<", u ? i = +i + 1 : a = +a + 1), r === "<" && (c = "-0"), n = `${r + i}.${a}.${s}${c}`) : u ? n = `>=${i}.0.0${c} <${+i + 1}.0.0-0` : d && (n = `>=${i}.${a}.0${c} <${i}.${+a + 1}.0-0`), o("xRange return", n), n;
		});
	}, re = (e, t) => (o("replaceStars", e, t), e.trim().replace(c[l.STAR], "")), ie = (e, t) => (o("replaceGTE0", e, t), e.trim().replace(c[t.includePrerelease ? l.GTE0PRE : l.GTE0], "")), C = (e) => (t, n, r, i, a, o, s, c, l, u, d, f) => (n = y(r) ? "" : y(i) ? `>=${r}.0.0${e ? "-0" : ""}` : y(a) ? `>=${r}.${i}.0${e ? "-0" : ""}` : o ? `>=${n}` : `>=${n}${e ? "-0" : ""}`, c = y(l) ? "" : y(u) ? `<${+l + 1}.0.0-0` : y(d) ? `<${l}.${+u + 1}.0-0` : f ? `<=${l}.${u}.${d}-${f}` : e ? `<${l}.${u}.${+d + 1}-0` : `<=${c}`, `${n} ${c}`.trim()), ae = (e, t, n) => {
		for (let n = 0; n < e.length; n++) if (!e[n].test(t)) return !1;
		if (t.prerelease.length && !n.includePrerelease) {
			for (let n = 0; n < e.length; n++) if (o(e[n].semver), e[n].semver !== a.ANY && e[n].semver.prerelease.length > 0) {
				let r = e[n].semver;
				if (r.major === t.major && r.minor === t.minor && r.patch === t.patch) return !0;
			}
			return !1;
		}
		return !0;
	};
})), pa = /* @__PURE__ */ n(((e, t) => {
	var n = Symbol("SemVer ANY");
	t.exports = class e {
		static get ANY() {
			return n;
		}
		constructor(t, i) {
			if (i = r(i), t instanceof e) {
				if (t.loose === !!i.loose) return t;
				t = t.value;
			}
			t = t.trim().split(/\s+/).join(" "), s("comparator", t, i), this.options = i, this.loose = !!i.loose, this.parse(t), this.semver === n ? this.value = "" : this.value = this.operator + this.semver.version, s("comp", this);
		}
		parse(e) {
			let t = this.options.loose ? i[a.COMPARATORLOOSE] : i[a.COMPARATOR], r = e.match(t);
			if (!r) throw TypeError(`Invalid comparator: ${e}`);
			this.operator = r[1] === void 0 ? "" : r[1], this.operator === "=" && (this.operator = ""), r[2] ? this.semver = new c(r[2], this.options.loose) : this.semver = n;
		}
		toString() {
			return this.value;
		}
		test(e) {
			if (s("Comparator.test", e, this.options.loose), this.semver === n || e === n) return !0;
			if (typeof e == "string") try {
				e = new c(e, this.options);
			} catch {
				return !1;
			}
			return o(e, this.operator, this.semver, this.options);
		}
		intersects(t, n) {
			if (!(t instanceof e)) throw TypeError("a Comparator is required");
			return this.operator === "" ? this.value === "" ? !0 : new l(t.value, n).test(this.value) : t.operator === "" ? t.value === "" ? !0 : new l(this.value, n).test(t.semver) : (n = r(n), n.includePrerelease && (this.value === "<0.0.0-0" || t.value === "<0.0.0-0") || !n.includePrerelease && (this.value.startsWith("<0.0.0") || t.value.startsWith("<0.0.0")) ? !1 : !!(this.operator.startsWith(">") && t.operator.startsWith(">") || this.operator.startsWith("<") && t.operator.startsWith("<") || this.semver.version === t.semver.version && this.operator.includes("=") && t.operator.includes("=") || o(this.semver, "<", t.semver, n) && this.operator.startsWith(">") && t.operator.startsWith("<") || o(this.semver, ">", t.semver, n) && this.operator.startsWith("<") && t.operator.startsWith(">")));
		}
	};
	var r = Vi(), { safeRe: i, t: a } = Bi(), o = ua(), s = zi(), c = Q(), l = $();
})), ma = /* @__PURE__ */ n(((e, t) => {
	var n = $();
	t.exports = (e, t, r) => {
		try {
			t = new n(t, r);
		} catch {
			return !1;
		}
		return t.test(e);
	};
})), ha = /* @__PURE__ */ n(((e, t) => {
	var n = $();
	t.exports = (e, t) => new n(e, t).set.map((e) => e.map((e) => e.value).join(" ").trim().split(" "));
})), ga = /* @__PURE__ */ n(((e, t) => {
	var n = Q(), r = $();
	t.exports = (e, t, i) => {
		let a = null, o = null, s = null;
		try {
			s = new r(t, i);
		} catch {
			return null;
		}
		return e.forEach((e) => {
			s.test(e) && (!a || o.compare(e) === -1) && (a = e, o = new n(a, i));
		}), a;
	};
})), _a = /* @__PURE__ */ n(((e, t) => {
	var n = Q(), r = $();
	t.exports = (e, t, i) => {
		let a = null, o = null, s = null;
		try {
			s = new r(t, i);
		} catch {
			return null;
		}
		return e.forEach((e) => {
			s.test(e) && (!a || o.compare(e) === 1) && (a = e, o = new n(a, i));
		}), a;
	};
})), va = /* @__PURE__ */ n(((e, t) => {
	var n = Q(), r = $(), i = ia();
	t.exports = (e, t) => {
		e = new r(e, t);
		let a = new n("0.0.0");
		if (e.test(a) || (a = new n("0.0.0-0"), e.test(a))) return a;
		a = null;
		for (let t = 0; t < e.set.length; ++t) {
			let r = e.set[t], o = null;
			r.forEach((e) => {
				let t = new n(e.semver.version);
				switch (e.operator) {
					case ">": t.prerelease.length === 0 ? t.patch++ : t.prerelease.push(0), t.raw = t.format();
					case "":
					case ">=":
						(!o || i(t, o)) && (o = t);
						break;
					case "<":
					case "<=": break;
					/* istanbul ignore next */
					default: throw Error(`Unexpected operation: ${e.operator}`);
				}
			}), o && (!a || i(a, o)) && (a = o);
		}
		return a && e.test(a) ? a : null;
	};
})), ya = /* @__PURE__ */ n(((e, t) => {
	var n = $();
	t.exports = (e, t) => {
		try {
			return new n(e, t).range || "*";
		} catch {
			return null;
		}
	};
})), ba = /* @__PURE__ */ n(((e, t) => {
	var n = Q(), r = pa(), { ANY: i } = r, a = $(), o = ma(), s = ia(), c = aa(), l = la(), u = ca();
	t.exports = (e, t, d, f) => {
		e = new n(e, f), t = new a(t, f);
		let p, m, h, g, _;
		switch (d) {
			case ">":
				p = s, m = l, h = c, g = ">", _ = ">=";
				break;
			case "<":
				p = c, m = u, h = s, g = "<", _ = "<=";
				break;
			default: throw TypeError("Must provide a hilo val of \"<\" or \">\"");
		}
		if (o(e, t, f)) return !1;
		for (let n = 0; n < t.set.length; ++n) {
			let a = t.set[n], o = null, s = null;
			if (a.forEach((e) => {
				e.semver === i && (e = new r(">=0.0.0")), o ||= e, s ||= e, p(e.semver, o.semver, f) ? o = e : h(e.semver, s.semver, f) && (s = e);
			}), o.operator === g || o.operator === _ || (!s.operator || s.operator === g) && m(e, s.semver) || s.operator === _ && h(e, s.semver)) return !1;
		}
		return !0;
	};
})), xa = /* @__PURE__ */ n(((e, t) => {
	var n = ba();
	t.exports = (e, t, r) => n(e, t, ">", r);
})), Sa = /* @__PURE__ */ n(((e, t) => {
	var n = ba();
	t.exports = (e, t, r) => n(e, t, "<", r);
})), Ca = /* @__PURE__ */ n(((e, t) => {
	var n = $();
	t.exports = (e, t, r) => (e = new n(e, r), t = new n(t, r), e.intersects(t, r));
})), wa = /* @__PURE__ */ n(((e, t) => {
	var n = ma(), r = Qi();
	t.exports = (e, t, i) => {
		let a = [], o = null, s = null, c = e.sort((e, t) => r(e, t, i));
		for (let e of c) n(e, t, i) ? (s = e, o ||= e) : (s && a.push([o, s]), s = null, o = null);
		o && a.push([o, null]);
		let l = [];
		for (let [e, t] of a) e === t ? l.push(e) : !t && e === c[0] ? l.push("*") : t ? e === c[0] ? l.push(`<=${t}`) : l.push(`${e} - ${t}`) : l.push(`>=${e}`);
		let u = l.join(" || "), d = typeof t.raw == "string" ? t.raw : String(t);
		return u.length < d.length ? u : t;
	};
})), Ta = /* @__PURE__ */ n(((e, t) => {
	var n = $(), r = pa(), { ANY: i } = r, a = ma(), o = Qi(), s = (e, t, r = {}) => {
		if (e === t) return !0;
		e = new n(e, r), t = new n(t, r);
		let i = !1;
		OUTER: for (let n of e.set) {
			for (let e of t.set) {
				let t = u(n, e, r);
				if (i ||= t !== null, t) continue OUTER;
			}
			if (i) return !1;
		}
		return !0;
	}, c = [new r(">=0.0.0-0")], l = [new r(">=0.0.0")], u = (e, t, n) => {
		if (e === t) return !0;
		if (e.length === 1 && e[0].semver === i) {
			if (t.length === 1 && t[0].semver === i) return !0;
			e = n.includePrerelease ? c : l;
		}
		if (t.length === 1 && t[0].semver === i) {
			if (n.includePrerelease) return !0;
			t = l;
		}
		let r = /* @__PURE__ */ new Set(), s, u;
		for (let t of e) t.operator === ">" || t.operator === ">=" ? s = d(s, t, n) : t.operator === "<" || t.operator === "<=" ? u = f(u, t, n) : r.add(t.semver);
		if (r.size > 1) return null;
		let p;
		if (s && u && (p = o(s.semver, u.semver, n), p > 0 || p === 0 && (s.operator !== ">=" || u.operator !== "<="))) return null;
		for (let e of r) {
			if (s && !a(e, String(s), n) || u && !a(e, String(u), n)) return null;
			for (let r of t) if (!a(e, String(r), n)) return !1;
			return !0;
		}
		let m, h, g, _, v = u && !n.includePrerelease && u.semver.prerelease.length ? u.semver : !1, y = s && !n.includePrerelease && s.semver.prerelease.length ? s.semver : !1;
		v && v.prerelease.length === 1 && u.operator === "<" && v.prerelease[0] === 0 && (v = !1);
		for (let e of t) {
			if (_ = _ || e.operator === ">" || e.operator === ">=", g = g || e.operator === "<" || e.operator === "<=", s) {
				if (y && e.semver.prerelease && e.semver.prerelease.length && e.semver.major === y.major && e.semver.minor === y.minor && e.semver.patch === y.patch && (y = !1), e.operator === ">" || e.operator === ">=") {
					if (m = d(s, e, n), m === e && m !== s) return !1;
				} else if (s.operator === ">=" && !a(s.semver, String(e), n)) return !1;
			}
			if (u) {
				if (v && e.semver.prerelease && e.semver.prerelease.length && e.semver.major === v.major && e.semver.minor === v.minor && e.semver.patch === v.patch && (v = !1), e.operator === "<" || e.operator === "<=") {
					if (h = f(u, e, n), h === e && h !== u) return !1;
				} else if (u.operator === "<=" && !a(u.semver, String(e), n)) return !1;
			}
			if (!e.operator && (u || s) && p !== 0) return !1;
		}
		return !(s && g && !u && p !== 0 || u && _ && !s && p !== 0 || y || v);
	}, d = (e, t, n) => {
		if (!e) return t;
		let r = o(e.semver, t.semver, n);
		return r > 0 ? e : r < 0 || t.operator === ">" && e.operator === ">=" ? t : e;
	}, f = (e, t, n) => {
		if (!e) return t;
		let r = o(e.semver, t.semver, n);
		return r < 0 ? e : r > 0 || t.operator === "<" && e.operator === "<=" ? t : e;
	};
	t.exports = s;
})), Ea = /* @__PURE__ */ n(((e, t) => {
	var n = Bi(), r = Ri(), i = Q(), a = Hi();
	t.exports = {
		parse: Ui(),
		valid: Wi(),
		clean: Gi(),
		inc: Ki(),
		diff: qi(),
		major: Ji(),
		minor: Yi(),
		patch: Xi(),
		prerelease: Zi(),
		compare: Qi(),
		rcompare: $i(),
		compareLoose: ea(),
		compareBuild: ta(),
		sort: na(),
		rsort: ra(),
		gt: ia(),
		lt: aa(),
		eq: oa(),
		neq: sa(),
		gte: ca(),
		lte: la(),
		cmp: ua(),
		coerce: da(),
		Comparator: pa(),
		Range: $(),
		satisfies: ma(),
		toComparators: ha(),
		maxSatisfying: ga(),
		minSatisfying: _a(),
		minVersion: va(),
		validRange: ya(),
		outside: ba(),
		gtr: xa(),
		ltr: Sa(),
		intersects: Ca(),
		simplifyRange: wa(),
		subset: Ta(),
		SemVer: i,
		re: n.re,
		src: n.src,
		tokens: n.t,
		SEMVER_SPEC_VERSION: r.SEMVER_SPEC_VERSION,
		RELEASE_TYPES: r.RELEASE_TYPES,
		compareIdentifiers: a.compareIdentifiers,
		rcompareIdentifiers: a.rcompareIdentifiers
	};
})), Da, Oa, ka = t((() => {
	Da = /* @__PURE__ */ e(Ea(), 1), He(), Ue(), p(), s(), re(), i(), v(), we(), Oa = (e) => Da.default.valid(e) ?? Da.default.valid(Da.default.coerce(e)), window.Vaadin.copilot.eventbus.on("serverInfo", (e) => {
		let t = e.detail.versions.find((e) => e.name === "Vaadin");
		if (!t) return;
		let n = Ve.getMostRecentVaadinVersion(), i = t.version;
		u(`${ie}get-release-note-url`, { version: i }, (e) => {
			let t = e.data;
			if (!t.error && t.url) if (n) {
				let e = Oa(i), a = Oa(n), o = e !== null && a !== null ? Da.default.gte(e, a) : i === n;
				O.setProjectVersionReleaseNoteInfo({
					mostRecentVersion: o,
					vaadinVersion: i,
					url: t.url
				}), e !== null && a !== null && Da.default.gt(e, a) && (We({
					type: r.INFORMATION,
					message: `You're now on version ${i}!`,
					details: ne(b`<a class="ahreflike" href="${t.url}" target="_blank">Click here to open release notes!</a>`)
				}), Ve.setMostRecentReleaseNoteDismissed(!1), Ve.setMostRecentVaadinVersion(i));
			} else Ve.setMostRecentReleaseNoteDismissed(!1), Ve.setMostRecentVaadinVersion(i), O.setProjectVersionReleaseNoteInfo({
				mostRecentVersion: !0,
				vaadinVersion: i,
				url: t.url
			});
		});
	});
}));
//#endregion
t((() => {
	Tt(), g(), At(), Ce(), Nt(), Pt(), Rt(), Bt(), Wt(), Kt(), un(), pn(), Di(), Fi(), Li(), ka(), _e(), D.on("copilot-java-after-update", (e) => {
		e.detail.classes.find((e) => e.routePath !== void 0) && D.emit("update-routes", {});
	});
}))();
