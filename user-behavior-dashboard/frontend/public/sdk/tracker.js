/**
 * 用户行为追踪SDK
 * 使用方法：在网站中引入 <script src="http://localhost:81/sdk/tracker.js"></script>
 */
(function() {
  'use strict';

  // 配置
  var config = {
    serverUrl: 'http://localhost:8081/api/track',
    appId: 'default',
    userId: null,
    sessionId: generateSessionId(),
    debug: false
  };

  // 生成会话ID
  function generateSessionId() {
    return 'sess_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
  }

  // 生成用户ID（未登录用户）
  function generateVisitorId() {
    var stored = localStorage.getItem('_tracker_uid');
    if (stored) return stored;
    var id = 'visitor_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
    localStorage.setItem('_tracker_uid', id);
    return id;
  }

  // 获取设备类型
  function getDeviceType() {
    var ua = navigator.userAgent;
    if (/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(ua)) {
      return 'MOBILE';
    }
    if (/iPad|Tablet/i.test(ua)) {
      return 'TABLET';
    }
    return 'PC';
  }

  // 获取地域（通过IP，这里先用默认值）
  function getRegion() {
    return '未知';
  }

  // 发送数据
  function send(data) {
    var payload = {
      appId: config.appId,
      userId: config.userId || generateVisitorId(),
      sessionId: config.sessionId,
      deviceType: getDeviceType(),
      region: getRegion(),
      url: window.location.href,
      referrer: document.referrer,
      timestamp: Date.now(),
      data: data
    };

    if (config.debug) {
      console.log('[Tracker]', payload);
    }

    // 使用 sendBeacon 发送（不阻塞页面）
    if (navigator.sendBeacon) {
      navigator.sendBeacon(config.serverUrl, JSON.stringify(payload));
    } else {
      // 降级使用 XMLHttpRequest
      var xhr = new XMLHttpRequest();
      xhr.open('POST', config.serverUrl, true);
      xhr.setRequestHeader('Content-Type', 'application/json');
      xhr.send(JSON.stringify(payload));
    }
  }

  // 追踪页面浏览
  function trackPageView() {
    send({
      eventType: 'PAGE_VIEW',
      page: window.location.pathname,
      title: document.title
    });
  }

  // 追踪点击事件
  function trackClick(element, extra) {
    send({
      eventType: 'CLICK',
      page: window.location.pathname,
      element: element.tagName,
      text: element.textContent ? element.textContent.substring(0, 50) : '',
      href: element.href || '',
      extra: extra || {}
    });
  }

  // 追踪自定义事件
  function trackEvent(eventName, data) {
    send({
      eventType: eventName,
      page: window.location.pathname,
      data: data || {}
    });
  }

  // 追踪购买事件
  function trackPurchase(orderId, amount, products) {
    send({
      eventType: 'PURCHASE',
      page: window.location.pathname,
      orderId: orderId,
      amount: amount,
      products: products || []
    });
  }

  // 追踪搜索事件
  function trackSearch(keyword) {
    send({
      eventType: 'SEARCH',
      page: window.location.pathname,
      keyword: keyword
    });
  }

  // 初始化
  function init(options) {
    if (options) {
      for (var key in options) {
        if (options.hasOwnProperty(key)) {
          config[key] = options[key];
        }
      }
    }

    // 自动追踪页面浏览
    trackPageView();

    // 自动追踪点击事件
    document.addEventListener('click', function(e) {
      var target = e.target;
      // 追踪按钮和链接点击
      if (target.tagName === 'BUTTON' || target.tagName === 'A' ||
          target.closest('button') || target.closest('a')) {
        trackClick(target);
      }
    });

    // 追踪页面停留时间
    var startTime = Date.now();
    window.addEventListener('beforeunload', function() {
      var duration = Math.round((Date.now() - startTime) / 1000);
      trackEvent('PAGE_STAY', { duration: duration });
    });

    if (config.debug) {
      console.log('[Tracker] 初始化完成', config);
    }
  }

  // 暴露全局 API
  window.Tracker = {
    init: init,
    trackPageView: trackPageView,
    trackClick: trackClick,
    trackEvent: trackEvent,
    trackPurchase: trackPurchase,
    trackSearch: trackSearch,
    config: config
  };

  // 自动初始化
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function() { init(); });
  } else {
    init();
  }
})();
