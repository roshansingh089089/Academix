import{ChevronLeft,ChevronRight}from'lucide-react';

export default function Pagination({page=1,total=0,size=50,onPageChange}){
 const totalPages=Math.max(1,Math.ceil(total/size));
 const start=total===0?0:(page-1)*size+1;
 const end=Math.min(page*size,total);
 const first=Math.max(1,Math.min(page-2,totalPages-4));
 const pages=Array.from({length:Math.min(5,totalPages)},(_,i)=>first+i);
 return <div className="pagination"><span>Showing <strong>{start}</strong> to <strong>{end}</strong> of <strong>{Number(total).toLocaleString('en-IN')}</strong> students</span><div><button disabled={page<=1} onClick={()=>onPageChange?.(page-1)}><ChevronLeft size={15}/> Previous</button>{pages.map(n=><button key={n} className={n===page?'selected':''} onClick={()=>onPageChange?.(n)}>{n}</button>)}{totalPages>pages.at(-1)&&<i>...</i>}<button disabled={page>=totalPages} onClick={()=>onPageChange?.(page+1)}>Next <ChevronRight size={15}/></button></div></div>
}
