import{useEffect,useId,useMemo,useRef,useState}from'react';
import{Check,Search,X}from'lucide-react';

export default function SearchableSelect({label,value='',options=[],onChange}){
 const id=useId(),root=useRef(null),[open,setOpen]=useState(false),[active,setActive]=useState(-1);
 const selected=options.some(option=>option.toLowerCase()===value.toLowerCase());
 const matches=useMemo(()=>selected?options:options.filter(option=>option.toLowerCase().includes(value.trim().toLowerCase())),[options,selected,value]);
 useEffect(()=>{const close=e=>{if(!root.current?.contains(e.target)){setOpen(false);setActive(-1)}};document.addEventListener('pointerdown',close);return()=>document.removeEventListener('pointerdown',close)},[]);
 useEffect(()=>setActive(-1),[value,options]);
 const choose=option=>{onChange(option);setOpen(false);setActive(-1)};
 const keyDown=e=>{
  if(e.key==='ArrowDown'){e.preventDefault();setOpen(true);setActive(index=>Math.min(index+1,matches.length-1))}
  else if(e.key==='ArrowUp'){e.preventDefault();setOpen(true);setActive(index=>Math.max(index-1,0))}
  else if(e.key==='Enter'){if(open&&active>=0&&matches[active]){e.preventDefault();choose(matches[active])}else setOpen(false)}
  else if(e.key==='Escape'){setOpen(false);setActive(-1)}
  else if(e.key==='Tab')setOpen(false);
 };
 return <div className={`combo-field ${open?'open':''}`} ref={root}><label htmlFor={id}>{label}</label><div className="combo-input"><Search size={14}/><input id={id} role="combobox" aria-expanded={open} aria-controls={`${id}-list`} aria-autocomplete="list" value={value} placeholder={`All ${label==='Class'?'Classes':`${label}s`}`} onFocus={()=>setOpen(true)} onClick={()=>setOpen(true)} onKeyDown={keyDown} onChange={e=>{onChange(e.target.value);setOpen(true)}}/>{value&&<button type="button" className="combo-clear" aria-label={`Clear ${label}`} onMouseDown={e=>e.preventDefault()} onClick={()=>{onChange('');setOpen(true)}}><X size={13}/></button>}</div>{open&&<div id={`${id}-list`} role="listbox" className="combo-menu">{matches.length?matches.map((option,index)=><button type="button" role="option" aria-selected={option.toLowerCase()===value.toLowerCase()} className={`${index===active?'highlighted':''} ${option.toLowerCase()===value.toLowerCase()?'selected':''}`} key={option} onMouseDown={e=>e.preventDefault()} onMouseEnter={()=>setActive(index)} onClick={()=>choose(option)}><span>{option}</span>{option.toLowerCase()===value.toLowerCase()&&<Check size={14}/>}</button>):<div className="combo-empty">No matching options</div>}</div>}</div>
}
