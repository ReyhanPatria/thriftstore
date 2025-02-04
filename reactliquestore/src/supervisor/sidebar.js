import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemText from '@mui/material/ListItemText';
import Typography from '@mui/material/Typography';
import { Collapse, ListSubheader } from '@mui/material';
import { ExpandLess, ExpandMore, LocalShippingOutlined, PersonOutlined, ShoppingBagOutlined } from '@mui/icons-material';
import { Link, useLocation } from 'react-router-dom';
import { useEffect, useState } from 'react';

function Sidebar() {
  const location = useLocation();
  const [openKaryawan, setOpenKaryawan] = useState(false);
  const [openStok, setOpenStok] = useState(false);
  const [openOrderDelivery, setopenOrderDelivery] = useState(false);

  const handleKaryawanClick = () => {
    setOpenKaryawan(!openKaryawan);
  };
  const handleStokClick = () => {
    setOpenStok(!openStok);
  };
  const handleOrderDeliveryClick = () => {
    setopenOrderDelivery(!openOrderDelivery);
  };

  useEffect(() => {
    if (location.pathname.startsWith('/supervisor/karyawan')) {
      setOpenKaryawan(true);
    }
    if (location.pathname.startsWith('/supervisor/stok')) {
      setOpenStok(true);
    }
    if (location.pathname.startsWith('/supervisor/orderDelivery')) {
      setopenOrderDelivery(true);
    }
  }, [location.pathname]);

  return (
    <div>
      <List
        component="nav"
        aria-labelledby="nested-list-subheader"
        subheader={
          <ListSubheader component="div" id="nested-list-subheader" style={{ color: 'white', backgroundColor: 'black' }}>
            <Typography fontSize={50} color={'#FE8A01'}>Supervisor</Typography>
          </ListSubheader>
        }
      >
        <ListItem button onClick={handleKaryawanClick}>
          <PersonOutlined />&nbsp;&nbsp;&nbsp;
          <ListItemText primary="Karyawan" />
          {openKaryawan ? <ExpandLess /> : <ExpandMore />}
        </ListItem>
        <Collapse in={openKaryawan} timeout="auto" unmountOnExit>
          <List component="div" disablePadding style={{ paddingLeft: 20 }}>
            <ListItem button component={Link} to="/supervisor/karyawan/presensi">
              <ListItemText primary="Absen" />
            </ListItem>
          </List>
        </Collapse>
        <ListItem button onClick={handleStokClick}>
          <ShoppingBagOutlined />&nbsp;&nbsp;&nbsp;
          <ListItemText primary="Stok" />
          {openStok ? <ExpandLess /> : <ExpandMore />}
        </ListItem>
        <Collapse in={openStok} timeout="auto" unmountOnExit>
          <List component="div" disablePadding style={{ paddingLeft: 20 }}>
            <ListItem button component={Link} to="/supervisor/stok/reviewStok">
              <ListItemText primary="Review Stok" />
            </ListItem>
            <ListItem button component={Link} to="/supervisor/stok/tipeBarang">
              <ListItemText primary="Tipe Barang" />
            </ListItem>
          </List>
        </Collapse>
        <ListItem button onClick={handleOrderDeliveryClick}>
          <LocalShippingOutlined />&nbsp;&nbsp;&nbsp;
          <ListItemText primary="Pemesanan dan Pengiriman" />
          {openOrderDelivery ? <ExpandLess /> : <ExpandMore />}
        </ListItem>
        <Collapse in={openOrderDelivery} timeout="auto" unmountOnExit>
          <List component="div" disablePadding style={{ paddingLeft: 20 }}>
            <ListItem button component={Link} to="/supervisor/orderDelivery/pemesanan">
              <ListItemText primary="Input Pemesanan" />
            </ListItem>
            <ListItem button component={Link} to="/supervisor/orderDelivery/pengiriman">
              <ListItemText primary="Input Pengiriman" />
            </ListItem>
            <ListItem button component={Link} to="/supervisor/orderDelivery/reviewOrderDelivery">
              <ListItemText primary="Review Pemesanan dan Pengiriman" />
            </ListItem>
          </List>
        </Collapse>
      </List>
    </div>
  );
}

export default Sidebar;
