import { n as e } from "./chunk-DiqZc92J.js";
import { $ as t, et as n, n as r, o as i, r as a, t as o, u as s } from "./icons-Daxdm7Mp.js";
import { a as c, d as l, i as u, l as d, o as f, r as p, s as m } from "./section-panel-ui-state-ByeyD6ou.js";
import { n as h, r as g } from "./copilot-ui-state-DjlBBpyf.js";
import { r as _, t as v } from "./stats-D_j8yo6z.js";
import { c as y, l as b, o as x, r as S, u as C } from "./copilot-error-handler-B7az3wX2.js";
import { n as w, t as T } from "./copilot-stored-machine-state-DS3t0BPl.js";
import { n as E, r as D } from "./copilot-notification-BnxWYvla.js";
import { n as O, t as k } from "./base-panel-Cq1wQn0M.js";
//#region frontend/copilot/shared/copilot-experimental-features.ts
var A, j, M, N, P, F, I, L, R = e((() => {
	T(), g(), A = (e) => h.userInfo?.copilotExperimentalFeatureFlag === !0 && w.isExperimentalFeatureEnabled(e), j = {
		id: "theme-from-image",
		name: "Theme from Image",
		description: "Generate a custom theme based on an image you provide.",
		enabled: () => A(j),
		available: () => h.appTheme === "lumo",
		requiresReload: !1
	}, M = {
		id: "ai-docs-assistant",
		name: "AI Docs Assistant",
		description: "AI-powered Vaadin documentation assistant.",
		enabled: () => A(M),
		available: () => !0,
		requiresReload: !1
	}, N = {
		id: "testbench-test-recorder",
		name: "TestBench Test Recorder",
		description: "Record user interactions to generate end-to-end Vaadin TestBench tests automatically.",
		enabled: () => A(N),
		available: () => !0,
		requiresReload: !0
	}, P = {
		id: "i18n",
		name: "Internationalization",
		description: "Edit and manage translations for your application.",
		enabled: () => A(P),
		available: () => !0,
		requiresReload: !0
	}, F = {
		id: "annotations",
		name: "Annotations",
		description: "Add and manage comments and annotations on your application views.",
		enabled: () => A(F),
		available: () => !0,
		requiresReload: !0
	}, I = {
		id: "ui-test-generator",
		name: "UI Test Generator",
		description: "Generate Playwright UI Test for your application views.",
		enabled: () => A(I),
		available: () => !0,
		requiresReload: !0
	}, L = [
		j,
		M,
		N,
		P,
		F,
		I
	];
})), z, B, V, H;
//#endregion
e((() => {
	x(), v(), a(), m(), R(), E(), n(), T(), g(), y(), r(), u(), O(), f(), z = window.Vaadin.devTools, B = class extends k {
		constructor(...e) {
			super(...e), this.toggledFeaturesThatAreRequiresServerRestart = [];
		}
		connectedCallback() {
			super.connectedCallback(), this.classList.add("contents");
		}
		render() {
			let e = h.userInfo?.copilotExperimentalFeatureFlag;
			return s`
      <div class="flex flex-col gap-6 px-4 py-0.5">
        <div class="border-dashed flex flex-col divide-y">
          ${h.featureFlags.slice().sort((e, t) => e.title.localeCompare(t.title)).map((e) => s`
                <div class="flex gap-2 justify-between py-3.5">
                  <div class="flex flex-col">
                    <label id="${e.id}-label">${e.title}</label>
                    <a
                      class="flex gap-0.5 text-xs"
                      href="${e.moreInfoLink}"
                      id="${e.id}-desc"
                      target="_blank"
                      rel="noopener noreferrer"
                      >More info<vaadin-icon class="icon-sm" .svg="${o.arrowOutward}"></vaadin-icon
                    ></a>
                  </div>
                  <copilot-toggle-button
                    accessible-name-ref="${e.id}-label"
                    accessible-desc-ref="${e.id}-desc"
                    ?checked=${e.enabled}
                    @on-change=${(t) => this.toggleFeatureFlag(t, e)}>
                  </copilot-toggle-button>
                </div>
              `)}
        </div>
        <div class="flex flex-col gap-1">
          ${e ? s`<h3 class="font-semibold my-0 text-sm">Copilot Experimental Features</h3>
                <div class="border-dashed flex flex-col divide-y">
                  ${L.filter((e) => e.available()).slice().sort((e, t) => e.name.localeCompare(t.name)).map((e) => s`
                        <div class="flex gap-2 justify-between py-3.5">
                          <div class="flex flex-col">
                            <label id="${e.id}-label">${e.description}</label>
                          </div>
                          <copilot-toggle-button
                            accessible-name-ref="${e.id}-label"
                            ?checked=${w.isExperimentalFeatureEnabled(e)}
                            @on-change=${(t) => this.toggleExperimentalFeatureFlag(t, e)}>
                          </copilot-toggle-button>
                        </div>
                      `)}
                </div>` : i}
        </div>
      </div>
    `;
		}
		toggleFeatureFlag(e, n) {
			let r = e.target.checked;
			_("use-feature", {
				source: "toggle",
				enabled: r,
				id: n.id
			}), z.frontendConnection ? (z.frontendConnection.send("setFeature", {
				featureId: n.id,
				enabled: r
			}), n.requiresServerRestart && h.toggleServerRequiringFeatureFlag(n), D({
				type: t.INFORMATION,
				message: `“${n.title}” ${r ? "enabled" : "disabled"}`,
				details: n.requiresServerRestart ? S() : void 0,
				dismissId: `feature${n.id}${r ? "Enabled" : "Disabled"}`
			}), n.id === "copilotExperimentalFeatures" && h.userInfo && h.setUserInfo({
				...h.userInfo,
				copilotExperimentalFeatureFlag: r
			})) : z.log("error", `Unable to toggle feature ${n.title}: No server connection available`);
		}
		toggleExperimentalFeatureFlag(e, t) {
			let n = e.target.checked;
			_("use-experimental-feature", {
				source: "toggle",
				enabled: n,
				id: t.id
			});
			let r = w.isExperimentalFeatureEnabled(t);
			w.setExperimentalFeatureEnabled(t, n), t.requiresReload && n && !r && window.location.reload();
		}
	}, c([d()], B.prototype, "toggledFeaturesThatAreRequiresServerRestart", void 0), B = c([l("copilot-features-panel")], B), V = class extends p {
		constructor(...e) {
			super(...e), this.serverRestarting = !1;
		}
		createRenderRoot() {
			return this;
		}
		render() {
			if (h.serverRestartRequiringToggledFeatureFlags.length === 0 || !C()) return i;
			let e = this.serverRestarting ? "Restarting..." : "Click to restart server";
			return s`
      <vaadin-button
        aria-label="Restart server"
        ?disabled="${this.serverRestarting}"
        theme="icon tertiary"
        @click=${() => {
				this.serverRestarting = !0, b();
			}}>
        <vaadin-icon .svg="${o.refresh}"></vaadin-icon>
        <vaadin-tooltip slot="tooltip" text=${e}></vaadin-tooltip>
      </vaadin-button>
    `;
		}
	}, c([d()], V.prototype, "serverRestarting", void 0), V = c([l("copilot-features-actions")], V), H = {
		header: "Features",
		tag: "copilot-features-panel",
		helpUrl: "https://vaadin.com/docs/latest/flow/configuration/feature-flags",
		actionsTag: "copilot-features-actions",
		toolbarOptions: {
			allowedModesWithOrder: { common: 0 },
			iconKey: "listAlt"
		}
	}, window.Vaadin.copilot.plugins.push({ init(e) {
		e.addPanel(H);
	} });
}))();
export { V as CopilotFeaturesActions, B as CopilotFeaturesPanel };
