const hostname = window.location.hostname;

const apiUrl = hostname.endsWith('.app.github.dev')
  ? `https://${hostname.replace('-4200.', '-8080.')}/`
  : 'http://localhost:8080/';

export const environment = {
  production: false,
  apiUrl,
};