import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  vus: 50,           // virtual users
  duration: '30s',   // total test duration
};

export default function () {
  let res = http.post('http://localhost:8080/api/v1/search/flights', JSON.stringify({
    sourceAirportId: 1,
    destinationAirportId: 2,
    date: "2024-04-01",
    passengers: 1,
    sortBy: "price"
  }), {
    headers: { 'Content-Type': 'application/json' },
  });

  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  sleep(1);
}
