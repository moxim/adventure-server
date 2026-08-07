import { n as e } from "./chunk-DiqZc92J.js";
import { L as t, Q as n, R as r, et as i, n as a, o, r as s, t as c, u as l } from "./icons-Daxdm7Mp.js";
import { l as u, o as d } from "./consts-PqkJ5Usg.js";
import { a as f, d as p, l as m, o as h, s as g } from "./section-panel-ui-state-ByeyD6ou.js";
import { n as _ } from "./copilot-eventbus-tR06exIW.js";
import { n as v, r as y } from "./copilot-ui-state-DjlBBpyf.js";
import { r as b, t as x } from "./stats-D_j8yo6z.js";
import { t as S } from "./directive-CZ105jp5.js";
import { a as C, d as w, f as T, o as E } from "./copilot-error-handler-B7az3wX2.js";
import { n as D, t as O } from "./copilot-stored-machine-state-DS3t0BPl.js";
import { a as k, i as A } from "./copilot-shortcuts-CnjqvktQ.js";
import { n as j, t as M } from "./base-panel-Cq1wQn0M.js";
import { n as N, r as P, t as F } from "./lit-renderer-Ca0EZVmm.js";
//#region node_modules/@vaadin/select/src/lit/renderer-directives.js
var I, L, R = e((() => {
	P(), N(), I = class extends F {
		addRenderer() {
			this.element.renderer = (e, t) => {
				this.renderRenderer(e, t);
			};
		}
		runRenderer() {
			this.element.requestContentUpdate();
		}
		removeRenderer() {
			this.element.renderer = null;
		}
	}, L = S(I);
})), z = e((() => {
	R();
})), B, V = e((() => {
	if (_(), B = window.Vaadin.copilot.tree, !B) throw Error("Tried to access copilot tree before it was initialized.");
})), H, U;
//#endregion
e((() => {
	g(), s(), a(), i(), w(), z(), A(), V(), O(), y(), j(), t(), u(), E(), x(), h(), H = class extends M {
		constructor(...e) {
			super(...e), this.selectedTab = 0, this.activationShortcutEnabled = D.isActivationShortcut(), this.aiUsage = D.isAIUsageAllowed(), this.sendErrorReportsAllowed = D.isSendErrorReportsAllowed(), this.hideCopilotRequestOngoing = !1, this.hideCopilotDialogVisible = !1, this.sizeItems = [{
				label: "Default",
				value: "default"
			}, {
				label: "Compact",
				value: "compact"
			}], this.themeItems = [
				{
					label: "System",
					value: "system"
				},
				{
					label: "Light",
					value: "light"
				},
				{
					label: "Dark",
					value: "dark"
				}
			], this.toolbarExpandModeItems = [
				{
					label: "Proximity",
					value: "proximity",
					description: "The toolbar expands and becomes fully visible as the mouse pointer approaches it."
				},
				{
					label: "Click",
					value: "click",
					description: "The toolbar expands and becomes fully visible when Play mode is clicked."
				},
				{
					label: "Hover",
					value: "hover",
					description: "The toolbar expands and becomes fully visible when the mouse hovers over it."
				},
				{
					label: "Always expanded",
					value: "always",
					description: "The toolbar remains fully visible at all times and never collapses or becomes translucent."
				},
				{
					label: "Disabled",
					value: "never",
					description: "Only Play mode is visible. Changing Copilot mode is not available, and keyboard shortcuts are disabled."
				}
			], this.badgePositionItems = [{
				label: "Smart",
				value: "smart",
				description: "Automatically finds the best position by avoiding overlaps with nearby elements."
			}, {
				label: "Static",
				value: "static",
				description: "Keeps the badge in a predefined position regardless of surrounding elements."
			}], this.aiUsageItems = [
				{
					label: "Ask each time",
					value: "ask"
				},
				{
					label: "Allow",
					value: "yes"
				},
				{
					label: "Deny",
					value: "no"
				}
			], this.aiProviderItems = [{
				label: "Any region",
				value: "ANY"
			}, {
				label: "EU only",
				value: "EU_ONLY"
			}], this.toggleActivationShortcut = () => {
				this.activationShortcutEnabled = !this.activationShortcutEnabled, D.setActivationShortcut(this.activationShortcutEnabled);
			}, this.toggleSendErrorReports = () => {
				this.sendErrorReportsAllowed = !this.sendErrorReportsAllowed, D.setSendErrorReportsAllowed(this.sendErrorReportsAllowed);
			};
		}
		connectedCallback() {
			super.connectedCallback(), this.classList.add("flex", "flex-col", "h-full");
		}
		updated(e) {
			super.updated(e);
		}
		renderKbd(e) {
			return T(e.replace(/<kbd([^>]*)class="([^"]*)"/, "<kbd$1class=\"$2 font-sans ms-auto\"").replace(/<kbd(?![^>]*class=)/, "<kbd class=\"font-sans ms-auto\""));
		}
		render() {
			return l`
      <vaadin-tabs>
        <vaadin-tab ?selected=${this.selectedTab === 0} @click=${() => this.selectedTab = 0}>General</vaadin-tab>
        <vaadin-tab ?selected=${this.selectedTab === 1} @click=${() => this.selectedTab = 1}>Shortcuts</vaadin-tab>
        <vaadin-tab ?selected=${this.selectedTab === 2} @click=${() => this.selectedTab = 2}>AI</vaadin-tab>
      </vaadin-tabs>
      ${this.selectedTab === 0 ? this.renderGeneralTab() : null}
      ${this.selectedTab === 1 ? this.renderShortcutsTab() : null} ${this.selectedTab === 2 ? this.renderAiTab() : null}
    `;
		}
		renderGeneralTab() {
			let e = D.getSelectedSize(), t = D.getSelectedTheme(), n = D.getToolbarExpandMode(), r = D.getBadgePositionMode();
			return l`
      <div class="border-dashed flex flex-col flex-grow divide-y pb-4 pt-0.5 px-4" part="general-tab-container">
        <div class="flex gap-2 items-start justify-between py-2">
          <label class="py-1.5" id="size">Size</label>
          <vaadin-select
            accessible-name-ref="size"
            class="flex-shrink-0"
            theme="auto-width no-border"
            .items="${this.sizeItems}"
            .value="${e}"
            @change="${(e) => {
				D.setSelectedSize(e.target.value);
			}}"></vaadin-select>
        </div>
        <div class="flex gap-2 items-start justify-between py-2">
          <label class="py-1.5" id="theme">Theme</label>
          <vaadin-select
            accessible-name-ref="theme"
            class="flex-shrink-0"
            theme="auto-width no-border"
            .items="${this.themeItems}"
            .value="${t}"
            @change="${(e) => {
				D.setSelectedTheme(e.target.value);
			}}"></vaadin-select>
        </div>
        <div class="flex gap-2 items-start justify-between py-2">
          <div class="flex flex-col py-1.5">
            <label id="toolbar-button-expand-mode">Toolbar behavior</label>
            <span class="text-secondary text-xs">How it appears & expands</span>
          </div>
          <vaadin-select
            accessible-name-ref="toolbar-expand-mode"
            class="flex-shrink-0"
            theme="auto-width no-border"
            .value="${n}"
            ${L(() => l`
                <vaadin-list-box class="max-w-xs">
                  ${this.toolbarExpandModeItems.map((e) => l`
                      <vaadin-item class="items-start" label="${e.label}" value="${e.value}">
                        <span class="flex flex-col gap-0.5">
                          <span>${e.label}</span>
                          <span class="text-secondary text-xs">${e.description}</span>
                        </span>
                      </vaadin-item>
                    `)}
                </vaadin-list-box>
              `)}
            @change="${(e) => {
				let t = D.getToolbarExpandMode();
				D.setToolbarExpandMode(e.target.value), b("toolbar-expand-mode-change", {
					selected: D.getToolbarExpandMode(),
					previous: t
				});
			}}"></vaadin-select>
        </div>
        <div class="flex gap-2 items-start justify-between py-2">
          <div class="flex flex-col py-1.5">
            <label id="toolbar-button-expand-mode">Badge positioning</label>
            <span class="text-secondary text-xs">How it is placed</span>
          </div>
          <vaadin-select
            accessible-name-ref="badge-position-mode"
            class="flex-shrink-0"
            theme="auto-width no-border"
            .value="${r}"
            ${L(() => l`
                <vaadin-list-box class="max-w-xs">
                  ${this.badgePositionItems.map((e) => l`
                      <vaadin-item class="items-start" label="${e.label}" value="${e.value}">
                        <span class="flex flex-col gap-0.5">
                          <span>${e.label}</span>
                          <span class="text-secondary text-xs">${e.description}</span>
                        </span>
                      </vaadin-item>
                    `)}
                </vaadin-list-box>
              `)}
            @change="${(e) => {
				let t = D.getBadgePositionMode();
				D.setBadgePositionMode(e.target.value), b("badge-position-mode-changed", {
					selected: D.getBadgePositionMode(),
					previous: t
				});
			}}"></vaadin-select>
        </div>
        <div class="flex gap-2 justify-between mb-4 py-3.5">
          <div class="flex flex-col">
            <label id="error-reports-label">Send error reports</label>
            <span id="error-reports-desc" class="text-secondary text-xs">Helps us improve the user experience</span>
          </div>
          <button
            aria-checked="${this.sendErrorReportsAllowed}"
            aria-labelledby="error-reports-label"
            aria-describedby="error-reports-desc"
            class="my-px"
            role="switch"
            type="button"
            @click=${() => this.toggleSendErrorReports()}>
            <span></span>
          </button>
        </div>
        <vaadin-button
          data-test-id="hide-copilot-btn"
          @click="${this.handleHideCopilotButtonClick}"
          class="justify-start mt-auto self-start">
          <vaadin-icon slot="prefix" .svg="${c.close}"></vaadin-icon>
          Hide Copilot until server restart
        </vaadin-button>
      </div>

      <vaadin-confirm-dialog
        id="hideCopilotDialog"
        header="Temporarily Hide Copilot"
        .confirmText=${this.hideCopilotRequestOngoing ? "Hiding…" : "Continue"}
        cancel-text="Cancel"
        cancel-button-visible
        confirm-theme="primary"
        .confirmDisabled=${this.hideCopilotRequestOngoing}
        .cancelDisabled=${this.hideCopilotRequestOngoing}
        .noCloseOnEsc=${this.hideCopilotRequestOngoing}
        .opened="${this.hideCopilotDialogVisible}"
        .noCloseOnOutsideClick=${this.hideCopilotRequestOngoing}
        @cancel=${() => {
				this.hideCopilotDialogVisible = !1;
			}}
        @confirm=${this.onDisableConfirm}>
        This will hide the Copilot until the server restarts. The page will reload to apply the change. Do you want to
        continue?
        ${this.hideCopilotRequestOngoing ? l`
              <div style="display:flex; align-items:center; gap:var(--lumo-space-s); margin-top:var(--lumo-space-m);">
                <vaadin-progress-indicator indeterminate></vaadin-progress-indicator>
                <span>Hiding…</span>
              </div>
            ` : null}
      </vaadin-confirm-dialog>
    `;
		}
		renderShortcutsTab() {
			let e = B.hasFlowComponents();
			return l`<div class="flex flex-col gap-4 pb-2 pt-4 px-4 ">
      <div class="flex justify-between">
        <div class="flex flex-col">
          <label id="enable-shortcuts-label">Enable keyboard shortcut</label>
          <span id="enable-shortcuts-desc" class="text-secondary text-xs"
            >Switch anytime to Play mode with ${this.renderKbd(k.toggleCopilot)}</span
          >
        </div>
        <button
          aria-checked="${this.activationShortcutEnabled}"
          aria-labelledby="enable-shortcuts-label"
          aria-describedby="enable-shortcuts-desc"
          class="my-px"
          role="switch"
          type="button"
          @click=${() => this.toggleActivationShortcut()}>
          <span></span>
        </button>
      </div>
      <div class="flex flex-col gap-1">
        <h3 class="font-semibold my-0 text-sm">Global</h3>
        <ul class="border-dashed divide-y flex flex-col list-none m-0 p-0">
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${c.vaadin}"></vaadin-icon>
            <span>Switch Play Mode / Last Mode</span>
            ${this.renderKbd(k.toggleCopilot)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${c.undo}"></vaadin-icon>
            <span>Undo</span>
            ${this.renderKbd(k.undo)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${c.redo}"></vaadin-icon>
            <span>Redo</span>
            ${this.renderKbd(k.redo)}
          </li>
        </ul>
      </div>
      <div class="flex flex-col gap-1">
        <h3 class="font-semibold my-0 text-sm">Component Selection</h3>
        <ul class="border-dashed divide-y flex flex-col list-none m-0 p-0">
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${c.sparkles}"></vaadin-icon>
            <span>Open AI prompt</span>
            ${this.renderKbd(k.openAiPopover)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${c.code}"></vaadin-icon>
            <span>Go to source</span>
            ${this.renderKbd(k.goToSource)}
          </li>
          ${e ? l`<li class="flex gap-2 py-2">
                <vaadin-icon .svg="${c.code}"></vaadin-icon>
                <span>Go to attach source</span>
                ${this.renderKbd(k.goToAttachSource)}
              </li>` : o}
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${c.contentCopy}"></vaadin-icon>
            <span>Copy</span>
            ${this.renderKbd(k.copy)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${c.contentPaste}"></vaadin-icon>
            <span>Paste</span>
            ${this.renderKbd(k.paste)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${c.fileCopy}"></vaadin-icon>
            <span>Duplicate</span>
            ${this.renderKbd(k.duplicate)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${c.turnLeft}"></vaadin-icon>
            <span>Select parent</span>
            ${this.renderKbd(k.selectParent)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${c.north}"></vaadin-icon>
            <span>Select previous sibling</span>
            ${this.renderKbd(k.selectPreviousSibling)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${c.south}"></vaadin-icon>
            <span>Select first child / next sibling</span>
            ${this.renderKbd(k.selectNextSibling)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${c.delete}"></vaadin-icon>
            <span>Delete</span>
            ${this.renderKbd(k.delete)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${c.dashboardCustomize}"></vaadin-icon>
            <span>Add component</span>
            <kbd class="font-sans ms-auto">A – Z</kbd>
          </li>
        </ul>
      </div>
    </div>`;
		}
		renderAiTab() {
			let e = v.userInfo?.copilotProjectCannotLeaveLocalhost ?? !1, t = e ? "no" : this.aiUsage, n = v.userInfo?.copilotProjectCannotLeaveEU ? "EU_ONLY" : "ANY";
			return l`<div class="border-dashed flex flex-col divide-y px-4 py-0.5">
      <div class="flex gap-2 items-start justify-between py-2">
        <div class="flex flex-col py-1.5">
          <label id="ai-usage">AI usage</label>
          <span class="text-secondary text-xs">All AI features are clearly labelled </span>
          ${e ? l`<span class="text-secondary text-xs"
                >Restricted for your account. Ask your Vaadin account manager to change it.</span
              >` : o}
        </div>
        <vaadin-select
          accessible-name-ref="ai-usage"
          class="flex-shrink-0"
          theme="auto-width no-border"
          .items="${this.aiUsageItems}"
          .value="${t}"
          ?disabled="${e}"
          @value-changed="${(t) => {
				e || (this.aiUsage = t.detail.value, D.setAIUsageAllowed(t.detail.value));
			}}"></vaadin-select>
      </div>
      <div class="flex gap-2 items-start justify-between py-2">
        <div class="flex flex-col py-1.5">
          <label id="ai-provider">AI provider</label>
          <span class="text-secondary text-xs">Restricted at account level, contact Vaadin to modify it.</span>
        </div>
        <vaadin-select
          accessible-name-ref="ai-provider"
          class="flex-shrink-0"
          theme="auto-width no-border"
          .items="${this.aiProviderItems}"
          .value="${n}"
          disabled></vaadin-select>
      </div>
    </div>`;
		}
		handleHideCopilotButtonClick() {
			this.hideCopilotDialogVisible = !0;
		}
		onDisableConfirm() {
			this.hideCopilotRequestOngoing = !0, r(`${d}hide-copilot`, {}, (e) => {
				C(e.data, {}) || (this.hideCopilotRequestOngoing = !1, window.location.reload());
			});
		}
	}, f([m()], H.prototype, "selectedTab", void 0), f([m()], H.prototype, "activationShortcutEnabled", void 0), f([m()], H.prototype, "aiUsage", void 0), f([m()], H.prototype, "sendErrorReportsAllowed", void 0), f([m()], H.prototype, "hideCopilotRequestOngoing", void 0), f([m()], H.prototype, "hideCopilotDialogVisible", void 0), H = f([p("copilot-settings-panel")], H), U = {
		header: "Settings",
		tag: n.SETTINGS
	}, window.Vaadin.copilot.plugins.push({ init(e) {
		e.addPanel(U);
	} });
}))();
export { H as CopilotSettingsPanel, U as panelConfig };
