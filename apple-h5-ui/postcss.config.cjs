/* eslint-disable */
const pxToViewport = require('postcss-px-to-viewport-8-plugin');

module.exports = {
  plugins: [
    pxToViewport({
      unitToConvert: 'px',
      viewportWidth: 375, // Vant uses 375px baseline; if designs are 750 double pixels as needed
      unitPrecision: 5,
      propList: ['*'],
      viewportUnit: 'vw',
      fontViewportUnit: 'vw',
      selectorBlackList: ['.ignore-'],
      minPixelValue: 2,
      mediaQuery: false,
      exclude: [/node_modules\/(?!vant)/],
    }),
  ],
};
