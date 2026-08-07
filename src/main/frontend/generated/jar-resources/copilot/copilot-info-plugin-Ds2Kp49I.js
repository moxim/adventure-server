import { i as e, n as t } from "./chunk-DiqZc92J.js";
import { n, o as r, r as i, t as a, u as o } from "./icons-Daxdm7Mp.js";
import { a as s, d as c, i as l, l as u, n as d, o as f, r as p, s as m, t as h } from "./section-panel-ui-state-ByeyD6ou.js";
import { n as g, t as _ } from "./copilot-eventbus-tR06exIW.js";
import { n as v, r as y } from "./copilot-ui-state-DjlBBpyf.js";
import { c as b, r as x, s as S } from "./copilot-development-setup-user-guide-utils-BxGBMF0F.js";
import { n as C, t as w } from "./base-panel-Cq1wQn0M.js";
import { n as T, r as E, t as D } from "./copy-to-clipboard-B1yKQNyU.js";
//#region frontend/copilot/plugins/copilot-info/copilot-info-plugin.ts
function O(e, t) {
	let n;
	return n = e === !0 ? "text-teal-11" : e === "partial" ? "text-amber-11" : "text-ruby-11", o`<span class="${n}">${t}</span>`;
}
var k, A, j, M;
//#endregion
t((() => {
	m(), i(), C(), y(), g(), l(), n(), k = /* @__PURE__ */ e(D(), 1), S(), h(), T(), f(), A = class extends w {
		constructor(...e) {
			super(...e), this.sortedEntries = [];
		}
		connectedCallback() {
			super.connectedCallback(), this.classList.add("contents"), this.reaction(() => v.projectInfoEntries, () => {
				if (!v.projectInfoEntries) return;
				let e = [...v.projectInfoEntries, {
					name: "Development Workflow",
					value: ""
				}];
				e = e.filter((e) => e.name !== "Java Hotswap"), this.sortedEntries = e.sort((e, t) => e.name.localeCompare(t.name));
			}, { fireImmediately: !0 });
		}
		render() {
			return o` <div class="flex flex-col py-2 px-4">
      <dl class="border-dashed divide-y m-0">
        ${E(this.sortedEntries.filter((e) => e.name !== "Java Hotswap"), (e) => e.name, (e) => this.renderRow(e))}
      </dl>
    </div>`;
		}
		renderRow(e) {
			if (e.name === "Development Workflow") return this.renderDevelopmentWorkflowButton();
			let t = e.name === "IDE Plugin" && e.value === !0 && v.idePluginState?.ide ? v.idePluginState.ide : e.value, n = this.getIcon(e.name, t), i = this.getIconColor(e.name), a = this.getTextColor(e);
			return o`
      <div class="flex gap-2 py-2">
        <dt class="flex gap-2">
          ${n ? o`<vaadin-icon class="${i}" .svg="${n}"></vaadin-icon>` : r} ${e.name}
        </dt>
        <dd class="flex gap-2 m-0 ${a}">${this.renderRowValue(e)}</dd>
      </div>
    `;
		}
		renderRowValue(e) {
			return e.name === "Vaadin Employee" && e.value === !0 ? o`
        <vaadin-icon id="vaadin-employee" class="text-teal-11" .svg="${a.check}"></vaadin-icon>
        <vaadin-tooltip for="vaadin-employee" text="Yes"></vaadin-tooltip>
      ` : o` ${!e.booleanInfo && typeof e.value == "string" ? e.value : r}
    ${e.booleanInfo && typeof e.value == "boolean" ? O(e.value, e.booleanInfo.ariaLabel) : r}
    ${e.booleanInfo?.text ? e.booleanInfo.text : r}
    ${e.name === "Vaadin" ? this.renderVaadinRowMore() : r}`;
		}
		renderVaadinRowMore() {
			let e = v.newVaadinVersionState?.versions !== void 0 && v.newVaadinVersionState.versions.length > 0;
			return o`
      ${v.projectVersionReleaseNoteInfo && v.projectVersionReleaseNoteInfo.url ? o`<a
            class="flex gap-0.5 items-center"
            href="${v.projectVersionReleaseNoteInfo.url}"
            id="release-notes-link"
            target="_blank"
            >Release notes <vaadin-icon class="icon-sm" .svg="${a.arrowOutward}"></vaadin-icon
          ></a>` : r}
      <vaadin-button
        aria-label="Edit Vaadin version"
        class="-my-1.5 relative"
        @click="${(e) => {
				e.stopPropagation(), d.openPanel("copilot-vaadin-versions");
			}}"
        id="new-vaadin-version-btn"
        theme="icon tertiary">
        <vaadin-icon .svg="${a.editSquare}"></vaadin-icon>
        <vaadin-tooltip slot="tooltip" text="Edit Vaadin version"></vaadin-tooltip>
        ${e ? o`<span aria-hidden="true" class="absolute bg-amber-11 end-0.5 rounded-full size-1 top-0.5"></span>` : ""}
      </vaadin-button>
    `;
		}
		renderDevelopmentWorkflowButton() {
			let e = x(), t = "", n = a.doneAll, r = "";
			return e.status === "success" ? (t = "text-teal-11", r = "IDE Plugin & Java Hotswap") : e.status === "warning" ? (t = "text-amber-11", n = a.arrowUploadReady, r = "Improve") : e.status === "error" && (t = "text-ruby-11", n = a.handyman, r = "Fix"), o`
      <div class="flex gap-2 py-2">
        <dt class="flex gap-2">
          <vaadin-icon class="text-amber-11" .svg="${a.bolt}"></vaadin-icon>
          Development Workflow
        </dt>
        <dd class="m-0">
          <vaadin-button
            class="-my-1.5 ${t}"
            id="development-workflow-status-detail"
            theme="tertiary"
            @click=${() => {
				b();
			}}>
            <vaadin-icon slot="prefix" .svg="${n}"></vaadin-icon>
            ${r}
          </vaadin-button>
        </dd>
      </div>
    `;
		}
		getIconColor(e) {
			return e.includes("Vaadin") || e === "Copilot" ? "text-vaadin-blue" : "";
		}
		getTextColor(e) {
			if (typeof e.value == "string") {
				if (e.value.startsWith("Enabled")) return "text-teal-11";
				if (e.value.startsWith("Disabled")) return "text-ruby-11";
			}
			return "text-secondary";
		}
		getIcon(e, t) {
			switch (e) {
				case "Browser": {
					let e = typeof t == "string" ? t.toLowerCase() : "";
					return e.includes("chrome") && !e.includes("edg") ? a.chrome : e.includes("firefox") ? a.firefox : e.includes("safari") && !e.includes("chrome") ? a.safari : e.includes("edg") ? a.edge : a.webAsset;
				}
				case "Copilot": return a.vaadin;
				case "Flow": return a.flow;
				case "Frontend Hotswap": return a.swapHoriz;
				case "Hilla": return a.hilla;
				case "Java": return a.java;
				case "OS": {
					let e = typeof t == "string" ? t.toLowerCase() : "";
					return e.includes("mac") ? a.apple : e.includes("win") ? a.windows : a.computer;
				}
				case "Spring": return a.spring;
				case "Spring Boot": return a.springBoot;
				case "Spring Data JPA": return a.springData;
				case "Spring Security": return a.springSecurity;
				case "Vaadin":
				case "Vaadin Employee": return a.vaadin;
				case "Java Hotswap": return a.swapHoriz;
				case "IDE Plugin": return typeof t == "string" ? t.toLowerCase() === "intellij" ? a.intelliJ : t.toLowerCase() === "vscode" ? a.vsCode : t.toLowerCase() === "eclipse" ? a.eclipse : a.developerModeTv : a.developerModeTv;
				default: return null;
			}
		}
	}, s([u()], A.prototype, "sortedEntries", void 0), A = s([c("copilot-info-panel")], A), j = class extends p {
		createRenderRoot() {
			return this;
		}
		connectedCallback() {
			super.connectedCallback(), this.style.display = "flex";
		}
		render() {
			return o` <vaadin-button
      aria-label="Copy to clipboard"
      @click=${() => {
				_.emit("system-info-with-callback", {
					callback: k.default,
					notify: !0
				});
			}}
      theme="icon tertiary">
      <vaadin-icon .svg="${a.fileCopy}"></vaadin-icon>
      <vaadin-tooltip slot="tooltip" text="Copy to clipboard"></vaadin-tooltip>
    </vaadin-button>`;
		}
	}, j = s([c("copilot-info-actions")], j), M = {
		header: "Info",
		tag: "copilot-info-panel",
		actionsTag: "copilot-info-actions",
		eager: !0,
		toolbarOptions: {
			iconKey: "info",
			allowedModesWithOrder: { common: 0 }
		}
	}, window.Vaadin.copilot.plugins.push({ init(e) {
		e.addPanel(M);
	} });
}))();
export { j as Actions, A as CopilotInfoPanel };
