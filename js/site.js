(function () {
  "use strict";

  function normalize(path) {
    return path.replace(/index\.html$/, "").replace(/\/?$/, "/");
  }

  // Mobile navigation
  var toggle = document.querySelector(".nav-toggle");
  var backdrop = document.querySelector(".nav-backdrop");
  function setNav(open) {
    document.body.classList.toggle("nav-open", open);
    if (toggle) toggle.setAttribute("aria-expanded", open ? "true" : "false");
  }
  if (toggle) toggle.addEventListener("click", function () {
    setNav(!document.body.classList.contains("nav-open"));
  });
  if (backdrop) backdrop.addEventListener("click", function () { setNav(false); });
  document.addEventListener("keydown", function (e) { if (e.key === "Escape") setNav(false); });

  // Highlight the current page in the sidebar, and build the previous/next links
  var here = normalize(location.pathname);
  var links = Array.prototype.filter.call(
    document.querySelectorAll(".side-nav a"),
    function (a) { return a.host === location.host; }
  );
  var current = -1;
  links.forEach(function (a, i) {
    if (normalize(a.pathname) === here) {
      a.classList.add("active");
      a.setAttribute("aria-current", "page");
      current = i;
    }
  });
  var pager = document.querySelector(".pager");
  if (pager && current >= 0) {
    var prev = links[current - 1], next = links[current + 1];
    if (prev) pager.insertAdjacentHTML("beforeend",
      '<a class="prev" href="' + prev.href + '"><span>Previous</span>' + prev.textContent + "</a>");
    if (next) pager.insertAdjacentHTML("beforeend",
      '<a class="next" href="' + next.href + '"><span>Next</span>' + next.textContent + "</a>");
    var active = links[current];
    var sidebar = document.querySelector(".sidebar");
    if (sidebar && active.offsetTop > sidebar.clientHeight - 60) {
      sidebar.scrollTop = active.offsetTop - sidebar.clientHeight / 2;
    }
  }

  // Code blocks: Clojure is highlighted, REPL output and terminal commands are labelled
  document.querySelectorAll("pre > code").forEach(function (code) {
    var pre = code.parentNode;
    var cls = code.className || "";
    var lang = (cls.match(/(?:language-)?(clojure|bash|shell|sh|text|plaintext)\b/) || [])[1];
    if (!lang || lang === "text" || lang === "plaintext") {
      pre.classList.add("output");
      return;
    }
    if (lang === "bash" || lang === "shell" || lang === "sh") {
      pre.classList.add("shell");
    } else if (window.hljs) {
      window.hljs.highlightElement(code);
    }
    if (navigator.clipboard) {
      var button = document.createElement("button");
      button.type = "button";
      button.className = "copy-button";
      button.textContent = "Copy";
      button.addEventListener("click", function () {
        navigator.clipboard.writeText(code.innerText.replace(/\n$/, "")).then(function () {
          button.textContent = "Copied";
          setTimeout(function () { button.textContent = "Copy"; }, 1500);
        });
      });
      pre.appendChild(button);
    }
  });

  // Wide tables scroll inside their own box
  document.querySelectorAll(".doc-body table, .home-body table").forEach(function (table) {
    if (table.parentNode.classList.contains("table-wrap")) return;
    var wrap = document.createElement("div");
    wrap.className = "table-wrap";
    table.parentNode.insertBefore(wrap, table);
    wrap.appendChild(table);
  });

  // Callouts: a blockquote that starts with "Tip" or "Watch out" gets its own colour
  document.querySelectorAll(".doc-body blockquote, .home-body blockquote").forEach(function (q) {
    var lead = q.querySelector("strong");
    var text = lead ? lead.textContent.toLowerCase() : "";
    if (/^tip/.test(text)) q.classList.add("tip");
    else if (/^(watch out|warning|careful|caution)/.test(text)) q.classList.add("warning");
  });

  // Link icons on headings
  document.querySelectorAll(".doc-body h2[id], .doc-body h3[id], .doc-body h4[id]").forEach(function (h) {
    var a = document.createElement("a");
    a.className = "anchor";
    a.href = "#" + h.id;
    a.setAttribute("aria-label", "Link to this section");
    a.textContent = "#";
    h.appendChild(a);
  });

  // Table of contents: collapsed on small screens, follows the reader on large ones
  var toc = document.querySelector(".page-toc");
  if (toc) {
    if (window.matchMedia("(max-width: 1239px)").matches && toc.querySelectorAll("a").length > 8) {
      toc.removeAttribute("open");
    }
    var tocLinks = {};
    toc.querySelectorAll("a[href^='#']").forEach(function (a) {
      tocLinks[decodeURIComponent(a.getAttribute("href").slice(1))] = a;
    });
    var headings = Array.prototype.filter.call(
      document.querySelectorAll(".doc-body h2[id], .doc-body h3[id], .doc-body h4[id]"),
      function (h) { return tocLinks[h.id]; }
    );
    var marked = null;
    var onScroll = function () {
      var top = 120, best = headings[0];
      headings.forEach(function (h) { if (h.getBoundingClientRect().top < top) best = h; });
      var link = best && tocLinks[best.id];
      if (link !== marked) {
        if (marked) marked.classList.remove("current");
        if (link) link.classList.add("current");
        marked = link;
      }
    };
    if (headings.length) {
      window.addEventListener("scroll", onScroll, { passive: true });
      onScroll();
    }
  }
})();
