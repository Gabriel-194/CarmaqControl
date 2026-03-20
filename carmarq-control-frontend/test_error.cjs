const puppeteer = require('puppeteer');
(async () => {
    const browser = await puppeteer.launch();
    const page = await browser.newPage();
    page.on('pageerror', err => console.log('PAGE ERROR:', err.toString()));
    page.on('console', msg => { if(msg.type() === 'error') console.log('CONSOLE ERROR:', msg.text()); });
    await page.goto('http://localhost:5173');
    await page.waitForSelector('input[type="email"]', { timeout: 5000 });
    await page.type('input[type="email"]', 'edu@gmail.com');
    await page.type('input[type="password"]', '123123');
    await page.click('button[type="submit"]');
    await page.waitForNavigation({ waitUntil: 'networkidle0' });
    console.log("Logged in");
    
    await page.goto('http://localhost:5173/nova-os');
    await new Promise(r => setTimeout(r, 2000));
    
    await browser.close();
})();
