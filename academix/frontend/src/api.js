import axios from'axios';
export const api=axios.create({baseURL:import.meta.env.VITE_API_URL||'http://localhost:8080/api',timeout:15000});
export const getStudents=params=>api.get('/students',{params}).then(r=>r.data);
export const getDashboard=()=>api.get('/dashboard/stats').then(r=>r.data);
export const uploadStudents=(file,onUploadProgress)=>{const data=new FormData();data.append('file',file);return api.post('/imports',data,{onUploadProgress}).then(r=>r.data)};
