// script.js

const expressionEl = document.getElementById("expression");
const resultEl = document.getElementById("result");
const keypad = document.querySelector(".keypad");
const historyList = document.getElementById("history-list");
const clearHistoryBtn = document.getElementById("clear-history");
const toggleThemeBtn = document.getElementById("toggle-theme");

let currentExpression = "";
let currentResult = 0;
let justEvaluated = false;

// Util: normaliza a expressão para avaliar com JS
function normalizeExpression(str) {
  return str
    .replace(/×/g, "*")
    .replace(/÷/g, "/")
    .replace(/−/g, "-")
    .replace(/,/g, ".");
}

function formatNumber(num) {
  if (Number.isNaN(num) || !Number.isFinite(num)) return "Erro";
  const asString = num.toString();
  if (asString.includes("e")) return num.toLocaleString("pt-BR");
  return num.toLocaleString("pt-BR", {
    maximumFractionDigits: 8,
  });
}

function updateDisplay() {
  expressionEl.textContent = currentExpression || "0";
  resultEl.textContent = formatNumber(currentResult);
}

// Cria card na timeline
function pushHistoryCard(expr, res) {
  const card = document.createElement("article");
  card.className = "history-card";

  const main = document.createElement("div");
  main.className = "history-main";

  const exprEl = document.createElement("div");
  exprEl.className = "history-expression";
  exprEl.textContent = expr;

  const row = document.createElement("div");
  row.className = "history-result-row";

  const resEl = document.createElement("div");
  resEl.className = "history-result";
  resEl.textContent = formatNumber(res);

  const meta = document.createElement("div");
  meta.className = "history-meta";
  const now = new Date();
  meta.textContent = now.toLocaleTimeString("pt-BR", {
    hour: "2-digit",
    minute: "2-digit",
  });

  row.appendChild(resEl);
  row.appendChild(meta);
  main.appendChild(exprEl);
  main.appendChild(row);
  card.appendChild(main);

  historyList.prepend(card);
}

function safeEvaluate() {
  if (!currentExpression) return;
  try {
    const normalized = normalizeExpression(currentExpression);
    const value = Function(`"use strict"; return (${normalized})`)();
    currentResult = value;
    updateDisplay();
    pushHistoryCard(currentExpression + " =", value);
    justEvaluated = true;
  } catch (e) {
    currentResult = NaN;
    updateDisplay();
  }
}

// Eventos da keypad
keypad.addEventListener("click", (e) => {
  const btn = e.target.closest("button");
  if (!btn) return;

  const num = btn.dataset.num;
  const op = btn.dataset.op;
  const action = btn.dataset.action;

  if (num !== undefined) {
    if (justEvaluated) {
      currentExpression = "";
      justEvaluated = false;
    }

    // vírgula: controla se já existe na parte atual
    if (num === ",") {
      const parts = currentExpression.split(/[\+\−\×\÷\%]/);
      const last = parts[parts.length - 1] || "";
      if (last.includes(",")) return;
      currentExpression += currentExpression ? "," : "0,";
    } else {
      currentExpression += num;
    }

    // prévia
    try {
      const normalized = normalizeExpression(currentExpression);
      const value = Function(`"use strict"; return (${normalized})`)();
      if (!Number.isNaN(value) && Number.isFinite(value)) {
        currentResult = value;
      }
    } catch {
      // ignora erros durante digitação
    }

    updateDisplay();
    return;
  }

  if (op !== undefined) {
    if (!currentExpression && op !== "%") return;
    if (justEvaluated) justEvaluated = false;

    // evita duplicar operadores
    if (/[\+\−\×\÷%]$/.test(currentExpression)) {
      currentExpression = currentExpression.slice(0, -1) + op;
    } else {
      currentExpression += op;
    }
    updateDisplay();
    return;
  }

  if (!action) return;

  switch (action) {
    case "clear":
      currentExpression = "";
      currentResult = 0;
      justEvaluated = false;
      updateDisplay();
      break;

    case "back":
      if (currentExpression.length > 0) {
        currentExpression = currentExpression.slice(0, -1);
        try {
          const normalized = normalizeExpression(currentExpression);
          const value = Function(`"use strict"; return (${normalized})`)();
          if (!Number.isNaN(value) && Number.isFinite(value)) {
            currentResult = value;
          } else {
            currentResult = 0;
          }
        } catch {
          currentResult = 0;
        }
        updateDisplay();
      }
      break;

    case "equals":
      safeEvaluate();
      break;

    case "sign":
      if (!currentExpression) {
        currentExpression = "-";
        updateDisplay();
        return;
      }
      // tenta aplicar ± ao último número
      const tokens = currentExpression.split(/([\+\−\×\÷%])/);
      let last = tokens[tokens.length - 1];
      if (!last) return;
      if (last.startsWith("-")) {
        last = last.slice(1);
      } else {
        last = "-" + last;
      }
      tokens[tokens.length - 1] = last;
      currentExpression = tokens.join("");
      try {
        const normalized = normalizeExpression(currentExpression);
        const value = Function(`"use strict"; return (${normalized})`)();
        if (!Number.isNaN(value) && Number.isFinite(value)) {
          currentResult = value;
        }
      } catch {
        //
      }
      updateDisplay();
      break;
  }
});

// Limpar histórico
clearHistoryBtn.addEventListener("click", () => {
  historyList.innerHTML = "";
});

// Tema claro/escuro
const root = document.documentElement;

function toggleTheme() {
  const isLight = root.getAttribute("data-theme") === "light";
  root.setAttribute("data-theme", isLight ? "dark" : "light");
  toggleThemeBtn.textContent = isLight ? "☾" : "☀︎";
}

toggleThemeBtn.addEventListener("click", toggleTheme);

// tema padrão
root.setAttribute("data-theme", "dark");

